package net.watchbox.domain.auth.repository;

import net.watchbox.domain.auth.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByOauthId(String oauthId);

    boolean existsByName(String name);
}
