package net.watchbox.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RedisRefreshTokenRepository {
    private static final String KEY_PREFIX = "refreshToken:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(
                key(memberId),
                refreshToken,
                jwtProperties.getRefreshTokenExpiry()
        );
    }

    public Optional<String> find(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(memberId)));
    }

    public void delete(Long memberId) {
        redisTemplate.delete(key(memberId));
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }
}
