package net.watchbox.domain.auth.entity;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.annotation.Id;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@RedisHash(value = "refreshToken", timeToLive = 3600 * 2) // 2시간
public class RefreshToken {
    @Id
    private Long memberId; // memberId 자체를 키로 사용 (한 멤버당 1토큰)

    private String refreshToken;

    public RefreshToken update(String newRefreshToken){
        this.refreshToken = newRefreshToken;
        return this;
    }
}