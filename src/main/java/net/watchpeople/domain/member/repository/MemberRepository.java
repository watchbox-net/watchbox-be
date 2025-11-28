package net.watchpeople.domain.member.repository;

import net.watchpeople.domain.account.entity.OauthAccount;
import net.watchpeople.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthAccount(OauthAccount oauthAccount);
}
