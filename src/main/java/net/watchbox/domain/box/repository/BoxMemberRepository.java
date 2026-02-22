package net.watchbox.domain.box.repository;

import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxMemberRepository extends JpaRepository<BoxMember, Long> {

    Optional<BoxMember> findByBoxAndMember(Box box, Member member);

    List<BoxMember> findAllByBox(Box box);

    List<BoxMember> findAllByMember(Member member);

    boolean existsByBoxAndMember(Box box, Member member);

    @Query("SELECT bm FROM BoxMember bm " +
            "JOIN FETCH bm.box b " +
            "WHERE bm.member = :member AND b.boxType = 'SHARED'")
    List<BoxMember> findWithSharedBoxByMember(@Param("member") Member member);
}
