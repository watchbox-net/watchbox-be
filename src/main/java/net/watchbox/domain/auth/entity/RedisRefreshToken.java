package net.watchbox.domain.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("refreshToken")
public class RedisRefreshToken {
    @Id
    private Long memberId;

    private String nickname;
    private String token;
    private String ip;
    private String deviceInfo;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant lastUsedAt;

    @TimeToLive
    private Long ttl;

    public void rotate(String newToken, Instant expiresAt, long ttlSeconds) {
        this.token = newToken;
        this.expiresAt = expiresAt;
        this.lastUsedAt = Instant.now();
        this.ttl = ttlSeconds;
    }
}
