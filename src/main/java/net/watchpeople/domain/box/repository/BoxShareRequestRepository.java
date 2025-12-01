package net.watchpeople.domain.box.repository;

import net.watchpeople.domain.box.entity.BoxShareRequest;
import net.watchpeople.domain.box.enums.RequestStatus;
import net.watchpeople.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoxShareRequestRepository extends JpaRepository<BoxShareRequest, Long> {

    List<BoxShareRequest> findByReceiverAndStatus(Member member, RequestStatus requestStatus);
}
