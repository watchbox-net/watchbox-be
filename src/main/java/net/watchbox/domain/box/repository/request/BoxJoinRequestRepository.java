package net.watchbox.domain.box.repository.request;

import net.watchbox.domain.box.entity.request.BoxJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxJoinRequestRepository extends JpaRepository<BoxJoinRequest, Long> {

}
