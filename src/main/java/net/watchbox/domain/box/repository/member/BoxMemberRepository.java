package net.watchbox.domain.box.repository.member;

import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxMemberRepository extends JpaRepository<BoxMember, Long> {

    Optional<BoxMember> findByBoxAndMember(Box box, Member member);

    List<BoxMember> findAllByBox(Box box);

    List<BoxMember> findAllByMember(Member member);

    boolean existsByBoxAndMember(Box box, Member member);

    @Query("SELECT bm FROM BoxMember bm " +
            "JOIN FETCH bm.box b " +
            "WHERE bm.member = :member AND b.boxType = 'SHARED'")
    List<BoxMember> findWithSharedBoxByMember(@Param("member") Member member);

    long countByMember(Member member);

    /**
     * 박스 멤버 중 특정 멤버를 제외한 member_id 목록.
     * 주로 알림 fan-out 수신자 목록 계산에 사용 (엔티티 hydration 없이 ID 컬럼만 select).
     */
    @Query("""
            SELECT bm.member.memberId
              FROM BoxMember bm
             WHERE bm.box = :box
               AND bm.member.memberId <> :excludedMemberId
            """)
    List<Long> findMemberIdsByBoxExcluding(@Param("box") Box box,
                                           @Param("excludedMemberId") Long excludedMemberId);
}
