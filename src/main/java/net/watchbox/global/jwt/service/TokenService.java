package net.watchbox.global.jwt.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    // 리프레시 토큰 발급
    public String createNewRefreshToken(Member member) {
        return tokenProvider.generateToken(member, jwtProperties.getRefreshTokenExpiry());
    }

    // 액세스 토큰 발급(by 리프레시 토큰)
    public String createNewAccessToken(Member member, String refreshToken){
        // 리프레시 토큰 유효성 검사
        if(!tokenProvider.validToken(refreshToken)){
            throw new IllegalArgumentException("유효한 리프레시 토큰이 아닙니다."); // 토큰 유효성 검사에 실패하면 예외 발생
        }
        // 사용자 ID로 사용자를 찾은 후에 토큰 제공자의 generateToken() 메서드를 호출해 새로운 액세스 토큰을 생성
        return tokenProvider.generateToken(member, jwtProperties.getAccessTokenExpiry());
    }
}
