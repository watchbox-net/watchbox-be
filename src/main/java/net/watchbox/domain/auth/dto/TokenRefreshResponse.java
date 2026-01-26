package net.watchbox.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenRefreshResponse {
    private String accessToken;
}
