package net.watchbox.domain.box.repository.request;

import net.watchbox.domain.box.entity.request.CreateBoxRequest;
import net.watchbox.domain.box.enums.RequestStatus;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreateBoxRequestRepository extends JpaRepository<CreateBoxRequest, Long> {

    List<CreateBoxRequest> findByReceiverAndStatus(Member member, RequestStatus requestStatus);
}
