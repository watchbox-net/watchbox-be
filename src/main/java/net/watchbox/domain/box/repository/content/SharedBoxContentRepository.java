package net.watchbox.domain.box.repository.content;

import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedBoxContentRepository extends JpaRepository<SharedBoxContent, Long> {
    boolean existsByAddedByAndContent(Member member, Content content);

}
