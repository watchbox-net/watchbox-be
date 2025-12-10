package net.watchbox.domain.box.repository.content;

import net.watchbox.domain.box.entity.content.SharedBoxContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedBoxContentRepository extends JpaRepository<SharedBoxContent, Long> {
}
