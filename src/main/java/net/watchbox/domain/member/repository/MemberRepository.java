package net.watchbox.domain.member.repository;

import net.watchbox.domain.auth.entity.OauthAccount;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthAccount(OauthAccount oauthAccount);

    Optional<Member> findByNickname(String nickname);

    Member findByOauthAccount_Name(String name);

    List<Member> findByNicknameContainingIgnoreCase(String query);

    @Query("""
        SELECT m.memberId as memberId,
               m.nickname as nickname,
               m.profileImage as profileImage,
               bi.status as status
        FROM Member m
        LEFT JOIN BoxInvitation bi
            ON bi.receiver.memberId = m.memberId
            AND bi.box.boxId = :boxId
        WHERE m.nickname LIKE %:query%
            AND m.memberId <> :excludeMemberId
    """)
    List<MemberInvitationProjection> findMembersWithInvitationStatus(
            @Param("query") String query,
            @Param("boxId") Long boxId,
            @Param("excludeMemberId") Long excludeMemberId
    );

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);


}
