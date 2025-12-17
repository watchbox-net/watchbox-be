package net.watchbox.domain.box.repository.request;

import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.entity.request.RequestStatus;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteBoxRequestRepository extends JpaRepository<InviteBoxRequest, Long> {

    List<InviteBoxRequest> findByReceiverAndStatus(Member member, RequestStatus requestStatus);
}
