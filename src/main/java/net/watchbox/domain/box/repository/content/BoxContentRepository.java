package net.watchbox.domain.box.repository.content;

import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoxContentRepository extends JpaRepository<BoxContent, Long> {
    boolean existsByAddedByAndContent(Member member, Content content);

    Long countByBox(Box box);

    List<BoxContent> findAllByBox(Box box);

    boolean existsByAddedByAndBoxAndContent(Member member, Box box, Content content);

    void deleteAllByBox(Box box);
}
