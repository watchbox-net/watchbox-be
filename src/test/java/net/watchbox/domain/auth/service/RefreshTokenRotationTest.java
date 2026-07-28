package net.watchbox.domain.auth.service;

import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.entity.RedisRefreshToken;
import net.watchbox.domain.auth.repository.RedisRefreshTokenRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.JwtProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 리프레시 토큰 회전의 <b>동시 요청 안전성</b> 검증.
 *
 * <p>액세스 토큰 만료 시 프론트가 동시에 여러 번 갱신하면, 먼저 처리된 요청이 토큰을 회전한 뒤
 * 뒤늦은 요청이 "직전 토큰"을 제출한다. 이때 재사용(탈취)으로 오판해 세션을 지우면 로그아웃되는데,
 * 유예시간(grace) 내라면 동시요청으로 보고 세션을 지켜야 한다. 유예시간을 벗어난 옛 토큰은 여전히
 * 재사용으로 차단한다.
 *
 * <p>로컬 Redis(6379) 필요.
 */
@DataRedisTest
@ContextConfiguration(classes = RefreshTokenRotationTest.TestApp.class)
class RefreshTokenRotationTest {

    private static final Long MEMBER_ID = 999_999_998L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final JwtProperties JWT =
            new JwtProperties("abc", "abcdefgh1234567890", Duration.ofDays(30), Duration.ofMinutes(30));

    @SpringBootConfiguration
    @EnableRedisRepositories(basePackageClasses = RedisRefreshTokenRepository.class)
    static class TestApp {
    }

    @Autowired
    private RedisRefreshTokenRepository repository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private TokenProvider tokenProvider;
    private TokenService tokenService;
    private Member member;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(JWT, null); // generateToken/validToken 은 memberRepository 미사용
        RefreshTokenSessionService sessionService = new RefreshTokenSessionService(repository, JWT);
        tokenService = new TokenService(tokenProvider, JWT, sessionService, redisTemplate);
        member = Member.builder().memberId(MEMBER_ID).email("t@example.com").nickname("테스트").build();
    }

    @AfterEach
    void cleanup() {
        repository.deleteById(MEMBER_ID);
        redisTemplate.delete("lock:refresh:" + MEMBER_ID);
    }

    /** 서로 다른 exp 로 항상 다른 JWT 문자열을 만든다(동일 초에도 구분되도록). */
    private String uniqueToken(long days) {
        return tokenProvider.generateToken(member, Duration.ofDays(days));
    }

    private void saveRotatedSession(String currentToken, String previousToken, OffsetDateTime rotatedAt) {
        repository.save(RedisRefreshToken.builder()
                .memberId(MEMBER_ID)
                .nickname("테스트")
                .token(currentToken)
                .previousToken(previousToken)
                .rotatedAt(rotatedAt)
                .issuedAt(rotatedAt)
                .expiresAt(rotatedAt.plusDays(30))
                .lastUsedAt(rotatedAt)
                .ttl(Duration.ofDays(30).toSeconds())
                .build());
    }

    @Test
    void 유예시간_내_직전토큰_동시요청은_재사용이_아니라_현재토큰으로_수렴한다() {
        String r0 = uniqueToken(30);
        String r1 = uniqueToken(31); // 이미 r0 → r1 로 회전된 상태
        saveRotatedSession(r1, r0, OffsetDateTime.now(KST));

        // 뒤늦은 동시요청이 직전 토큰 r0 을 제출
        TokenRefreshResponse res = tokenService.rotate(member, r0);

        assertThat(res.getRefreshToken()).isEqualTo(r1);        // 현재(이미 회전된) 토큰으로 수렴
        assertThat(repository.findById(MEMBER_ID)).isPresent(); // 세션 삭제 안 됨 → 로그아웃 안 됨
    }

    @Test
    void 유예시간_지난_옛토큰_재제출은_재사용으로_세션을_삭제한다() {
        String r0 = uniqueToken(30);
        String r1 = uniqueToken(31);
        // 회전이 5분 전에 일어남 → 유예(30초) 초과
        saveRotatedSession(r1, r0, OffsetDateTime.now(KST).minusMinutes(5));

        assertThatThrownBy(() -> tokenService.rotate(member, r0))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getCode())
                        .isEqualTo(ErrorCode.REFRESH_TOKEN_REUSED.getCode()));

        assertThat(repository.findById(MEMBER_ID)).isEmpty(); // 재사용 감지 → 세션 삭제
    }

    @Test
    void 현재토큰_정상회전은_새_토큰쌍을_발급하고_직전토큰을_보관한다() {
        String r0 = uniqueToken(30);
        OffsetDateTime now = OffsetDateTime.now(KST);
        saveRotatedSession(r0, null, now);

        TokenRefreshResponse res = tokenService.rotate(member, r0);

        assertThat(res.getAccessToken()).isNotBlank();
        assertThat(res.getRefreshToken()).isNotBlank();
        RedisRefreshToken after = repository.findById(MEMBER_ID).orElseThrow();
        assertThat(after.getPreviousToken()).isEqualTo(r0); // 직전 토큰 보관
        assertThat(after.getToken()).isEqualTo(res.getRefreshToken());
    }
}
