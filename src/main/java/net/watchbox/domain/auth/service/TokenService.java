package net.watchbox.domain.auth.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.entity.RedisRefreshToken;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenSessionService refreshTokenSessionService;

    public String createAccessToken(Member member) {
        return tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());
    }

    public String issueRefreshToken(Member member, String ip, String deviceInfo) {
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
        refreshTokenSessionService.save(member, refreshToken, ip, deviceInfo);
        return refreshToken;
    }

    public void logout(Long memberId) {
        refreshTokenSessionService.delete(memberId);
    }

    /**
     * 리프레시 토큰 회전(rotation).
     * 세션 메타데이터(ip, deviceInfo, issuedAt)는 유지하고 토큰·만료·활동시각만 갱신한다.
     */
    public TokenRefreshResponse rotate(Member member, String presentedRefreshToken) {
        if (!tokenProvider.validToken(presentedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = member.getMemberId();

        RedisRefreshToken stored = refreshTokenSessionService.find(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!stored.getToken().equals(presentedRefreshToken)) {
            refreshTokenSessionService.delete(memberId);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        String newAccessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());
        String newRefreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());

        long ttlSeconds = jwtProperties.refreshTokenExpiry().toSeconds();
        stored.rotate(newRefreshToken, Instant.now().plusSeconds(ttlSeconds), ttlSeconds);
        refreshTokenSessionService.save(stored);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }
}
