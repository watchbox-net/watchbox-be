package net.watchbox.domain.box.repository;

import net.watchbox.domain.box.entity.BoxMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxMemberRepository extends JpaRepository<BoxMember, Long> {

}
