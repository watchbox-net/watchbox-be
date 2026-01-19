package net.watchbox.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRefreshRequest {
    private String refreshToken;
}
