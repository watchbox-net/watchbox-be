package net.watchbox.domain.box.repository;

import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.BoxType;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {
    List<Box> findAllByOwnerAndBoxType(Member owner, BoxType boxType);

    /*
     SELECT b.*
     FROM box b
     JOIN box_member bm ON bm.box_id = b.id
     JOIN member m ON bm.member_id = m.id
     WHERE m.id = ?
     AND b.box_type = 'SHARED'

    boxMembers → Box의 boxMembers 컬렉션 타고 들어가서
    Member → BoxMember의 member 필드
    DB 쿼리가 한 번으로 끝나고, 불필요하게 BoxMember 레이어를 거칠 필요가 없어서 코드도 단순
     */
    List<Box> findAllByBoxMembers_MemberAndBoxType(Member member, BoxType boxType);
}
