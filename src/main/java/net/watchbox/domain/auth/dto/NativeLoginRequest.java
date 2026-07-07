package net.watchbox.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.watchbox.domain.auth.entity.OAuthProvider;

@Getter
@NoArgsConstructor
public class NativeLoginRequest {

    @NotNull
    @Schema(description = "소셜 provider (대문자)", example = "GOOGLE")
    private OAuthProvider provider;

    @NotBlank
    private String token; // Google: serverAuthCode, Apple: authorizationCode
}
