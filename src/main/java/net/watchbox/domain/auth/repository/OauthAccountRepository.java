package net.watchbox.domain.auth.repository;

import net.watchbox.domain.auth.entity.OauthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {
    Optional<OauthAccount> findByOauthId(String oauthId);

    boolean existsByName(String name);
}
