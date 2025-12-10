package net.watchbox.global.dev.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DevTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long oauthAccountId;
}
