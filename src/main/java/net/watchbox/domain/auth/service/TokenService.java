package net.watchbox.domain.auth.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.repository.RedisRefreshTokenRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RedisRefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(Member member) {
        return tokenProvider.generateToken(member, jwtProperties.getAccessTokenExpiry());
    }

    public String issueRefreshToken(Member member) {
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.getRefreshTokenExpiry());
        refreshTokenRepository.save(member.getMemberId(), refreshToken);
        return refreshToken;
    }

    public void logout(Long memberId) {
        refreshTokenRepository.delete(memberId);
    }

    /**
     * 리프레시 토큰 회전(rotation).
     * 1. 서명/만료 검증
     * 2. 저장된 토큰과 대조 — 불일치 시 재사용(탈취) 의심 → 체인 무효화 후 거부
     * 3. 일치 시 새 액세스/리프레시 토큰쌍 발급, 저장본을 새 리프레시 토큰으로 교체
     */
    public TokenRefreshResponse rotate(Member member, String presentedRefreshToken) {
        if (!tokenProvider.validToken(presentedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = member.getMemberId();

        String storedToken = refreshTokenRepository.find(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!storedToken.equals(presentedRefreshToken)) {
            refreshTokenRepository.delete(memberId);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        String newAccessToken = tokenProvider.generateToken(member, jwtProperties.getAccessTokenExpiry());
        String newRefreshToken = tokenProvider.generateToken(member, jwtProperties.getRefreshTokenExpiry());
        refreshTokenRepository.save(memberId, newRefreshToken);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }
}
