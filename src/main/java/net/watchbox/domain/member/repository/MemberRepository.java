package net.watchbox.domain.member.repository;

import net.watchbox.domain.auth.entity.OauthAccount;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthAccount(OauthAccount oauthAccount);

    Optional<Member> findByNickname(String nickname);
}
