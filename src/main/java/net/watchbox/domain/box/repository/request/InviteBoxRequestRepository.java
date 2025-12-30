package net.watchbox.domain.box.repository.request;

import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.entity.request.RequestStatus;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteBoxRequestRepository extends JpaRepository<InviteBoxRequest, Long> {
    List<InviteBoxRequest> findAllBySender(Member member);
    List<InviteBoxRequest> findAllByReceiver(Member member);

    List<InviteBoxRequest> findAllBySenderAndStatus(Member member, RequestStatus requestStatus);
    List<InviteBoxRequest> findAllByReceiverAndStatus(Member member, RequestStatus requestStatus);

    boolean existsBySharedBoxAndReceiverAndStatus(SharedBox sharedBox, Member receiver, RequestStatus requestStatus);
}
