package net.watchbox.domain.auth.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.RedisRefreshToken;
import net.watchbox.domain.auth.repository.RedisRefreshTokenRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenSessionService {
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    private final JwtProperties jwtProperties;

    public void save(Member member, String token, String ip, String deviceInfo) {
        Instant now = Instant.now();
        long ttlSeconds = jwtProperties.refreshTokenExpiry().toSeconds();
        redisRefreshTokenRepository.save(RedisRefreshToken.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .token(token)
                .ip(ip)
                .deviceInfo(deviceInfo)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .lastUsedAt(now)
                .ttl(ttlSeconds)
                .build());
    }

    public void save(RedisRefreshToken entity) {
        redisRefreshTokenRepository.save(entity);
    }

    public Optional<RedisRefreshToken> find(Long memberId) {
        return redisRefreshTokenRepository.findById(memberId);
    }

    public void delete(Long memberId) {
        redisRefreshTokenRepository.deleteById(memberId);
    }
}
