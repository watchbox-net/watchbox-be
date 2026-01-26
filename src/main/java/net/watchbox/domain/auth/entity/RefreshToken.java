package net.watchbox.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id", updatable = false)
    private Long refreshTokenId;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    public RefreshToken update(String newRefreshToken){
        this.refreshToken = newRefreshToken;
        return this;
    }
}