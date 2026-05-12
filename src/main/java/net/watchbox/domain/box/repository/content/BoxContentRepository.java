package net.watchbox.domain.box.repository.content;

import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxContentRepository extends JpaRepository<BoxContent, Long> {
    boolean existsByPublisherAndContent(Member publisher, Content content);

    Long countByBox(Box box);

    boolean existsByBoxAndContent(Box box, Content content);

    boolean existsByPublisherAndBoxAndContent(Member publisher, Box box, Content content);

    void deleteAllByBox(Box box);

    Optional<BoxContent> findByBoxAndContent(Box box, Content content);

    // 박스 컨텐츠 조회 시, SubContent인 영화/TV/인물 엔티티도 함께 조회
    @Query("SELECT bc FROM BoxContent bc " +
            "JOIN FETCH bc.content c " +
            "LEFT JOIN FETCH c.movie " +
            "LEFT JOIN FETCH c.tv " +
            "LEFT JOIN FETCH c.person " +
            "WHERE bc.box = :box " +
            "ORDER BY bc.createdAt DESC")
    List<BoxContent> findAllWithSubContentByBox(@Param("box") Box box);

    // 박스 리스트 조회 시 최근 포스터 경로 조회 (방법 A: JPQL + Java 그룹핑)
    @Query("SELECT bc.box.boxId AS boxId, " +
            "CASE WHEN bc.mediaType = net.watchbox.domain.content.entity.MediaType.MOVIE " +
            "     THEN m.posterPath ELSE t.posterPath END AS posterPath, " +
            "bc.createdAt AS createdAt " +
            "FROM BoxContent bc " +
            "JOIN bc.content c " +
            "LEFT JOIN c.movie m " +
            "LEFT JOIN c.tv t " +
            "WHERE bc.box IN :boxes " +
            "AND bc.mediaType IN (net.watchbox.domain.content.entity.MediaType.MOVIE, " +
            "                     net.watchbox.domain.content.entity.MediaType.TV) " +
            "ORDER BY bc.box.boxId, bc.createdAt DESC")
    List<BoxPosterProjection> findRecentPostersByBoxes(@Param("boxes") List<Box> boxes);

    // 특정 Content가 포함된 모든 박스 ID 목록 조회 (tmdbId + mediaType)
    @Query("SELECT bc.box.boxId FROM BoxContent bc " +
            "WHERE bc.content.tmdbId = :tmdbId " +
            "AND bc.content.mediaType = :mediaType")
    List<Long> findBoxIdsByContentTmdbIdAndMediaType(
            @Param("tmdbId") Long tmdbId,
            @Param("mediaType") MediaType mediaType);

//    @Query("SELECT bc.box.boxId FROM BoxContent bc WHERE bc.content.contentId = :contentId")
//    List<Long> findBoxIdsByContentId(@Param("contentId") Long contentId);

//    @Query("SELECT bc.box.boxId FROM BoxContent bc WHERE bc.content = :content")
//    List<Long> findBoxIdsByContent(@Param("content") Content content);

    // 특정 Content가 포함된 Member의 모든 박스 ID 목록 조회
    // - 박스 컨텐츠에 저장되었다는 것은 Content가 저장되어 있는 상태이므로 tmdbId가 아닌 contentId로 조회
    // - 공유 박스에서는 로그인한 유저가 추가한 컨텐츠만 포함으로 간주
    @Query("SELECT bc.box.boxId FROM BoxContent bc " +
            "WHERE bc.content = :content " +
            "AND (bc.box.boxType = 'MY' " +
            "     OR (bc.box.boxType = 'SHARED' AND bc.publisher = :member))")
    List<Long> findBoxIdsByContentForMember(
            @Param("content") Content content,
            @Param("member") Member member);

    void deleteAllByPublisherAndBox_BoxType(Member member, BoxType boxType);
}
