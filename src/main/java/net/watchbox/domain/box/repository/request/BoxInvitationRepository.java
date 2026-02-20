package net.watchbox.domain.box.repository.request;

import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.entity.invitation.RequestStatus;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoxInvitationRepository extends JpaRepository<BoxInvitation, Long> {
    List<BoxInvitation> findAllBySender(Member member);
    List<BoxInvitation> findAllByReceiver(Member member);

    List<BoxInvitation> findAllBySenderAndStatus(Member member, RequestStatus requestStatus);
    List<BoxInvitation> findAllByReceiverAndStatus(Member member, RequestStatus requestStatus);

    boolean existsByBoxAndReceiverAndStatus(Box box, Member receiver, RequestStatus requestStatus);
}
