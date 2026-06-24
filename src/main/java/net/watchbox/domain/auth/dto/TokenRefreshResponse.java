package net.watchbox.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenRefreshResponse {
    private String accessToken;
    // 회전(rotation)으로 새로 발급된 리프레시 토큰. 클라이언트는 이 값으로 기존 토큰을 교체 저장해야 한다.
    private String refreshToken;
}
