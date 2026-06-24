package net.watchbox.domain.auth.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.entity.RefreshToken;
import net.watchbox.domain.auth.repository.RefreshTokenRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    // 액세스 토큰 발급
    public String createAccessToken(Member member) {
        return tokenProvider.generateToken(member, jwtProperties.getAccessTokenExpiry());
    }

    // 리프레시 토큰 발급 + 저장(회원당 1개, 기존 토큰 교체)
    @Transactional
    public String issueRefreshToken(Member member) {
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.getRefreshTokenExpiry());
        saveRefreshToken(member.getMemberId(), refreshToken);
        return refreshToken;
    }

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    /**
     * 리프레시 토큰 회전(rotation).
     * 1. 서명/만료 검증
     * 2. 저장된 토큰과 대조 — 불일치 시 재사용(탈취) 의심 → 체인 무효화 후 거부
     * 3. 일치 시 새 액세스/리프레시 토큰쌍 발급, 저장본을 새 리프레시 토큰으로 교체
     */
    @Transactional
    public TokenRefreshResponse rotate(Member member, String presentedRefreshToken) {
        // 1. 리프레시 토큰 유효성 검사(서명/만료)
        if (!tokenProvider.validToken(presentedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = member.getMemberId();

        // 2. 저장된 토큰 조회 + 대조
        RefreshToken stored = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!stored.getRefreshToken().equals(presentedRefreshToken)) {
            // 저장본과 다른 토큰이 제시됨 = 이미 회전되어 폐기된 토큰의 재사용 → 탈취 의심
            // 해당 회원의 리프레시 토큰 체인을 무효화하여 강제 재로그인 유도
            refreshTokenRepository.deleteByMemberId(memberId);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        // 3. 회전: 새 토큰쌍 발급 + 저장본 교체(dirty checking)
        String newAccessToken = tokenProvider.generateToken(member, jwtProperties.getAccessTokenExpiry());
        String newRefreshToken = tokenProvider.generateToken(member, jwtProperties.getRefreshTokenExpiry());
        stored.update(newRefreshToken);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    // 리프레시 토큰 저장(upsert): 기존 엔티티가 있으면 교체, 없으면 신규 생성
    private void saveRefreshToken(Long memberId, String newRefreshToken) {
        RefreshToken entity = refreshTokenRepository.findByMemberId(memberId)
                .map(existing -> existing.update(newRefreshToken))
                .orElseGet(() -> RefreshToken.builder()
                        .memberId(memberId)
                        .refreshToken(newRefreshToken)
                        .build());
        refreshTokenRepository.save(entity);
    }
}
