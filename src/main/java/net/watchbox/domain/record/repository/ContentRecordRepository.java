package net.watchbox.domain.record.repository;

import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.ContentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ContentRecordRepository extends JpaRepository<ContentRecord, Long> {

    Optional<ContentRecord> findByMemberAndContent(Member member, Content content);

    List<ContentRecord> findByMemberAndWatchStatusIsNotNull(Member member);

    List<ContentRecord> findByMemberAndLiked(Member member, Boolean liked);
}
