package net.watchpeople.domain.box.repository;

import net.watchpeople.domain.box.entity.request.CreateBoxRequest;
import net.watchpeople.domain.box.enums.RequestStatus;
import net.watchpeople.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedBoxRequestRepository extends JpaRepository<CreateBoxRequest, Long> {

    List<CreateBoxRequest> findByReceiverAndStatus(Member member, RequestStatus requestStatus);
}
