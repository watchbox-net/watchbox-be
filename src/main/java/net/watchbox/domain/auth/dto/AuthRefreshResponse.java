package net.watchbox.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthRefreshResponse {
    private String accessToken;
}
