package net.watchpeople.domain.box.repository.content;

import net.watchpeople.domain.box.entity.content.SharedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedContentRepository extends JpaRepository<SharedContent, Long> {
}
