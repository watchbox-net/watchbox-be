package net.watchbox.domain.box.repository.invitation;

import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.entity.invitation.RequestStatus;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoxInvitationRepository extends JpaRepository<BoxInvitation, Long> {
    List<BoxInvitation> findAllBySender(Member member);
    List<BoxInvitation> findAllByReceiver(Member member);

    List<BoxInvitation> findAllBySenderAndStatus(Member member, RequestStatus requestStatus);
    List<BoxInvitation> findAllByReceiverAndStatus(Member member, RequestStatus requestStatus);

    boolean existsByBoxAndReceiverAndStatus(Box box, Member receiver, RequestStatus requestStatus);

    boolean existsByReceiverAndStatus(Member receiver, RequestStatus requestStatus);

    /**
     * 받은 초대 목록 조회 — 박스 정보 + 멤버까지 FETCH JOIN으로 한 번에 로딩
     * BoxInvitation에 매핑된 Box로 LAZY 관계를 개별 접근(getBox())하면 N+1 발생하므로 한 방 쿼리로 해결
     * - bi.box: 초대받은 박스
     * - bi.sender: 초대 보낸 사람 (닉네임용)
     * - b.boxMembers → bm.member: 박스에 속한 멤버 목록 + 멤버 정보
     * - LEFT JOIN: 멤버가 아직 없는 박스도 조회되도록
     */
    @Query("SELECT bi FROM BoxInvitation bi " +
            "JOIN FETCH bi.box b " +
            "JOIN FETCH bi.sender " +
            "LEFT JOIN FETCH b.boxMembers bm " +
            "LEFT JOIN FETCH bm.member " +
            "WHERE bi.receiver = :member and bi.status = 'PENDING'")
    List<BoxInvitation> findAllByReceiverWithBoxAndMembers(@Param("member") Member member);

}
