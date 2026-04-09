package net.watchbox.domain.box.repository.box;

import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {
    List<Box> findAllByOwnerAndBoxType(Member owner, BoxType boxType);

    /**
     * JPQL 마이 박스 + 공유 박스 통합 조회
     * - 마이 박스: owner = member AND boxType = MY
     * - 공유 박스: EXISTS 서브쿼리로 소속 여부 확인 (WHERE에서 boxMembers 필터링하면 다른 멤버 누락됨)
     * - LEFT JOIN FETCH로 공유 박스의 전체 멤버 목록 한 번에 로딩
     */
    @Query("SELECT DISTINCT b FROM Box b " +
            "LEFT JOIN FETCH b.boxMembers bm " +
            "LEFT JOIN FETCH bm.member " +
            "WHERE (b.owner = :member AND b.boxType = 'MY') " +
            "OR (b.boxType = 'SHARED' AND EXISTS (" +
            "    SELECT 1 FROM BoxMember bm2 WHERE bm2.box = b AND bm2.member = :member))")
    List<Box> findAllBoxesByMember(@Param("member") Member member);

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
//    List<Box> findAllByBoxMembers_MemberAndBoxType(Member member, BoxType boxType);
}
