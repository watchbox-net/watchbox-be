package net.watchbox.domain.auth.repository;

import net.watchbox.domain.auth.entity.RedisRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RedisRefreshTokenRepository extends CrudRepository<RedisRefreshToken, Long> {
}
