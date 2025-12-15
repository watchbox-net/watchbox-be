package net.watchbox.domain.box.repository.content;

import net.watchbox.domain.box.entity.content.MyBoxContent;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyBoxContentRepository extends JpaRepository<MyBoxContent, Long> {
    boolean existsByMemberAndContent(Member member, Content content);

    Optional<MyBoxContent> findByMemberAndContent(Member member, Content content);

    Optional<MyBoxContent> findByMemberAndContent_TmdbId(Member member, Long tmdbId);

    Long countByMember(Member member);

    List<MyBoxContent> findByMemberOrderByCreatedAtDesc(Member member);
}
