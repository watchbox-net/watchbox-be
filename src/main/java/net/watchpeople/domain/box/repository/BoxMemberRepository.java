package net.watchpeople.domain.box.repository;

import net.watchpeople.domain.box.entity.BoxMember;
import net.watchpeople.domain.box.enums.BoxType;
import net.watchpeople.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoxMemberRepository extends JpaRepository<BoxMember, Long> {
    Optional<BoxMember> findByMemberAndBox_BoxType(Member member, BoxType boxType);
}
