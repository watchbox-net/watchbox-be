package net.watchpeople.domain.box.repository.content;

import net.watchpeople.domain.box.entity.content.MyContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyContentRepository extends JpaRepository<MyContent, Long> {
}
