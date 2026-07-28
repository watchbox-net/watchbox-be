package net.watchbox.domain.auth.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.entity.RedisRefreshToken;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TokenService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 회전 직렬화용 락 (memberId 단위). 짧게 잡고, 최대 대기 시간 안에 못 얻으면 그대로 진행(락 TTL 로 자연 해제).
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final long LOCK_WAIT_MS = 5_000;
    // 방금 회전된 직전 토큰을 "동시요청"으로 인정해 주는 유예시간.
    private static final Duration ROTATE_GRACE = Duration.ofSeconds(30);
    // 락 소유자 검증 후 원자적 해제 (다른 요청이 잡은 락을 잘못 지우지 않도록)
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final StringRedisTemplate redis;

    public String createAccessToken(Member member) {
        return tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());
    }

    public String issueRefreshToken(Member member, String ip, String deviceInfo) {
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
        refreshTokenSessionService.save(member, refreshToken, ip, deviceInfo);
        return refreshToken;
    }

    /** 로그아웃. Redis 에 저장된 리프레시 토큰 세션을 삭제해 무효화한다. */
    public void logout(Long memberId) {
        refreshTokenSessionService.delete(memberId);
    }

    /**
     * 리프레시 토큰 회전(rotation) + 재사용 감지.
     *
     * <p>액세스 토큰 만료 시점에 프론트가 여러 요청(/auth/me + 데이터 프록시)으로 <b>동시에</b> 갱신을
     * 시도해도 강제 로그아웃되지 않도록 두 가지를 적용한다:
     * <ol>
     *   <li><b>memberId 단위 락</b>으로 회전을 직렬화 — 두 요청이 각자 다른 새 토큰을 만들어 세션이
     *       갈라지는 race 를 막는다.</li>
     *   <li><b>직전 토큰 유예(grace)</b> — 먼저 처리된 요청이 토큰을 회전한 뒤, 뒤늦게 들어온 요청이
     *       "직전 토큰"을 제출하면 재사용(탈취)이 아니라 동시요청으로 보고, 이미 회전된 현재 토큰으로
     *       수렴시킨다(액세스 토큰만 새로 발급).</li>
     * </ol>
     * 유예시간을 벗어난 옛 토큰이 다시 오면 그때는 진짜 재사용으로 판단해 세션을 삭제한다.
     */
    public TokenRefreshResponse rotate(Member member, String presentedRefreshToken) {
        if (!tokenProvider.validToken(presentedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = member.getMemberId();
        String lockKey = "lock:refresh:" + memberId;
        String lockOwner = UUID.randomUUID().toString();
        acquireLock(lockKey, lockOwner);
        try {
            RedisRefreshToken stored = refreshTokenSessionService.find(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
            OffsetDateTime now = OffsetDateTime.now(KST);

            // 1) 현재 토큰과 일치 → 정상 회전
            if (stored.getToken().equals(presentedRefreshToken)) {
                long ttlSeconds = jwtProperties.refreshTokenExpiry().toSeconds();
                String newAccessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());
                String newRefreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
                stored.rotate(newRefreshToken, now, now.plusSeconds(ttlSeconds), ttlSeconds);
                refreshTokenSessionService.save(stored);
                return new TokenRefreshResponse(newAccessToken, newRefreshToken);
            }

            // 2) 방금 회전된 직전 토큰 + 유예시간 내 → 동시요청. 현재(이미 회전된) 토큰으로 수렴.
            if (presentedRefreshToken.equals(stored.getPreviousToken())
                    && stored.getRotatedAt() != null
                    && now.isBefore(stored.getRotatedAt().plus(ROTATE_GRACE))) {
                String newAccessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());
                return new TokenRefreshResponse(newAccessToken, stored.getToken());
            }

            // 3) 그 외 → 진짜 재사용/탈취 → 세션 삭제
            refreshTokenSessionService.delete(memberId);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSED);
        } finally {
            releaseLock(lockKey, lockOwner);
        }
    }

    /** memberId 단위 락 획득. 최대 LOCK_WAIT_MS 동안 스핀. 못 얻어도 진행(락 TTL 로 자연 만료). */
    private void acquireLock(String key, String owner) {
        long deadline = System.currentTimeMillis() + LOCK_WAIT_MS;
        while (true) {
            Boolean acquired = redis.opsForValue().setIfAbsent(key, owner, LOCK_TTL);
            if (Boolean.TRUE.equals(acquired)) {
                return;
            }
            if (System.currentTimeMillis() >= deadline) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void releaseLock(String key, String owner) {
        redis.execute(new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class), List.of(key), owner);
    }
}
