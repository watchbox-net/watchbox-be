package net.watchbox.domain.box.repository;

import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxMemberRepository extends JpaRepository<BoxMember, Long> {

    Optional<BoxMember> findBySharedBoxAndMember(SharedBox sharedBox, Member member);

    List<BoxMember> findAllBySharedBox(SharedBox sharedBox);

    List<BoxMember> findAllByMember(Member member);
}
