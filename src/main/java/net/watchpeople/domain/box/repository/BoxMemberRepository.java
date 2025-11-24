package net.watchpeople.domain.box.repository;

import net.watchpeople.domain.box.entity.BoxMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxMemberRepository extends JpaRepository<BoxMember, Long> {
}
