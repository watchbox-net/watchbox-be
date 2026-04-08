package net.watchbox.domain.record.repository;

import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.ContentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ContentRecordRepository extends JpaRepository<ContentRecord, Long> {

    Optional<ContentRecord> findByMemberAndContent(Member member, Content content);

//    List<ContentRecord> findByMemberAndWatchStatusIsNotNull(Member member);

    @Query("SELECT cr FROM ContentRecord cr " +
            "JOIN FETCH cr.content c " +
            "LEFT JOIN FETCH c.movie " +
            "LEFT JOIN FETCH c.tv " +
            "LEFT JOIN FETCH c.person " +
            "WHERE cr.member = :member AND cr.watchStatus IS NOT NULL")
    List<ContentRecord> findWatchRecordsWithContent(@Param("member") Member member);

    @Query("SELECT cr FROM ContentRecord cr " +
            "JOIN FETCH cr.content c " +
            "LEFT JOIN FETCH c.movie " +
            "LEFT JOIN FETCH c.tv " +
            "LEFT JOIN FETCH c.person " +
            "WHERE cr.member = :member AND cr.liked = :liked")
    List<ContentRecord> findLikedRecordsWithContent(@Param("member") Member member, @Param("liked") Boolean liked);

//    @Query("SELECT cr FROM ContentRecord cr JOIN FETCH cr.content WHERE cr.member = :member AND cr.content.tmdbId IN :tmdbIds")
//    List<ContentRecord> findContentRecordsByMemberAndContentIdIn(@Param("member") Member member, @Param("tmdbIds") List<Long> tmdbIds);

    @Query("SELECT cr FROM ContentRecord cr JOIN FETCH cr.content " +
            "WHERE cr.member = :member " +
            "AND cr.content.tmdbId IN :tmdbIds " +
            "AND cr.content.mediaType = :mediaType")
    List<ContentRecord> findContentRecordsByMemberAndTmdbIdsAndMediaType(
            @Param("member") Member member,
            @Param("tmdbIds") List<Long> tmdbIds,
            @Param("mediaType") MediaType mediaType);

//    List<ContentRecord> findByMemberAndLiked(Member member, Boolean liked);

    @Query("SELECT cr FROM ContentRecord cr JOIN FETCH cr.content " +
            "WHERE cr.member = :member AND cr.content.contentId IN :contentIds")
    List<ContentRecord> findByMemberAndContentIdIn(
            @Param("member") Member member,
            @Param("contentIds") List<Long> contentIds);

    long countByMemberAndLikedTrue(Member member);

    long countByMemberAndWatchStatusIsNotNull(Member member);

    Optional<ContentRecord> findByMember_MemberIdAndContent_TmdbIdAndContent_MediaType(Long memberId, Long tmdbId, MediaType mediaType);
}
