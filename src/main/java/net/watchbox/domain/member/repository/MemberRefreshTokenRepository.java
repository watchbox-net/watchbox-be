package net.watchbox.domain.member.repository;

import net.watchbox.domain.member.entity.MemberRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRefreshTokenRepository extends JpaRepository<MemberRefreshToken, Long> {
    Optional<MemberRefreshToken> findByMemberId(Long memberId);
    Optional<MemberRefreshToken> findByRefreshToken(String refreshToken);
}
