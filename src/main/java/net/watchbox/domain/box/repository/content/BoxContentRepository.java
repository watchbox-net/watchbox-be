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
import net.watchbox.domain.box.entity.member.BoxMember;

@Repository
public interface BoxContentRepository extends JpaRepository<BoxContent, Long> {
    boolean existsByPublisherAndContent(Member publisher, Content content);

    Long countByBox(Box box);

    boolean existsByBoxAndContent(Box box, Content content);

    boolean existsByPublisherAndBoxAndContent(Member publisher, Box box, Content content);

    void deleteAllByBox(Box box);

    // 박스 콘텐츠 조회 시, SubContent인 영화/TV/인물 엔티티도 함께 조회
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
    // - 박스 콘텐츠에 저장되었다는 것은 Content가 저장되어 있는 상태이므로 tmdbId가 아닌 contentId로 조회
    // - 공유 박스에서는 로그인한 유저가 추가한 콘텐츠만 포함으로 간주
    @Query("SELECT bc.box.boxId FROM BoxContent bc " +
            "WHERE bc.content = :content " +
            "AND (bc.box.boxType = 'MY' " +
            "     OR (bc.box.boxType = 'SHARED' AND bc.publisher = :member))")
    List<Long> findBoxIdsByContentForMember(
            @Param("content") Content content,
            @Param("member") Member member);

    /**
     * 주어진 콘텐츠들 중 <b>이 박스에 담겨 있는 것</b>의 contentId 만 추린다.
     *
     * <p>포함 판정 기준은 {@link #findBoxIdsByContentForMember} 와 같다 —
     * MY 박스는 박스에 있으면 포함, SHARED 박스는 <b>내가 추가한 것만</b> 포함으로 본다.
     * 공유 박스에서 남이 담은 콘텐츠를 체크 상태로 보여주면, 체크를 풀었을 때
     * 삭제 권한이 없어 되돌아가는 화면이 된다.
     *
     * <p>한 페이지분 contentId 를 한 번에 넘겨 N+1 을 피한다.
     */
    @Query("SELECT bc.content.contentId FROM BoxContent bc " +
            "WHERE bc.box = :box " +
            "AND bc.content.contentId IN :contentIds " +
            "AND (bc.box.boxType = 'MY' " +
            "     OR (bc.box.boxType = 'SHARED' AND bc.publisher = :member))")
    List<Long> findContentIdsInBoxForMember(@Param("box") Box box,
                                            @Param("contentIds") List<Long> contentIds,
                                            @Param("member") Member member);

    /**
     * 삭제 대상 행. 공유 박스는 같은 콘텐츠를 여러 멤버가 각각 담을 수 있어
     * {@code (box, content)} 만으로는 행이 유일하지 않다. publisher 까지 포함해 특정한다.
     */
    Optional<BoxContent> findByBoxAndContentAndPublisher(Box box, Content content, Member publisher);

    /**
     * 주어진 tmdbId 중 <b>내가 담은 것</b>만 추린다.
     *
     * <p>홈·탐색 목록의 "박스에 담음" 표시용이다. 목록의 콘텐츠는 TMDB 응답이라
     * DB 에 Content 행이 없을 수도 있어 contentId 가 아닌 tmdbId 로 조회한다.
     *
     * <p>판정 기준은 <b>담은 사람(publisher)</b>이다. 공유 박스에서 다른 멤버가 담은 것은
     * 내 것으로 보지 않는다 — 상세 페이지({@code existsByPublisherAndContent})·시트와 같은 기준이라
     * 화면마다 아이콘이 달라지지 않는다. 마이 박스는 담을 수 있는 사람이 나뿐이라 자동으로 포함된다.
     */
    @Query("SELECT DISTINCT bc.content.tmdbId FROM BoxContent bc " +
            "WHERE bc.publisher = :member " +
            "AND bc.content.mediaType = :mediaType " +
            "AND bc.content.tmdbId IN :tmdbIds")
    List<Long> findTmdbIdsInMyBoxes(@Param("member") Member member,
                                    @Param("tmdbIds") List<Long> tmdbIds,
                                    @Param("mediaType") MediaType mediaType);

    void deleteAllByPublisherAndBox(Member publisher, Box box);

    void deleteAllByPublisherAndBox_BoxType(Member member, BoxType boxType);

    void deleteAllByPublisher(Member member);
}
