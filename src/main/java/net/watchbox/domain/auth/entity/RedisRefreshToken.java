//package net.watchbox.domain.auth.entity;
//
//import jakarta.persistence.Id;
//import org.springframework.data.redis.core.RedisHash;
//
//import java.time.LocalDateTime;
//
//@RedisHash(value = "refreshToken", timeToLive = 604800)
//public class RedisRefreshToken {
//    @Id
//    private Long memberId;
//    private String tokenHash;
//    private LocalDateTime expiresAt;
//    private boolean revoked;
//    private String deviceInfo;
//}
