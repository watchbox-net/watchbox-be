package net.watchbox.global.jwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessTokenCreateRequest {
    private String refreshToken;
}
