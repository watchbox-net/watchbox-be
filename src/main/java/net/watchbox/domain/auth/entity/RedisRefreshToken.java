package net.watchbox.domain.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.OffsetDateTime;

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
    // KST(+09:00) 기준 저장 — RedisInsight 등에서 원본값을 한국시간으로 바로 읽기 위함. 오프셋을 포함해 시점은 명확.
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastUsedAt;

    // 직전 리프레시 토큰 + 회전 시각.
    // 동시 갱신 요청 중 "늦게 도착한" 요청이 이미 회전된 이전 토큰을 제출해도, rotatedAt 기준
    // 유예시간 내라면 재사용(탈취)이 아니라 동시요청으로 판단해 세션을 지키기 위한 근거.
    private String previousToken;
    private OffsetDateTime rotatedAt;

    @TimeToLive
    private Long ttl;

    /** 리프레시 토큰 회전. 직전 토큰을 previousToken 에 보관하고 새 토큰·만료·활동시각으로 갱신. */
    public void rotate(String newToken, OffsetDateTime now, OffsetDateTime expiresAt, long ttlSeconds) {
        this.previousToken = this.token;
        this.token = newToken;
        this.expiresAt = expiresAt;
        this.lastUsedAt = now;
        this.rotatedAt = now;
        this.ttl = ttlSeconds;
    }
}
