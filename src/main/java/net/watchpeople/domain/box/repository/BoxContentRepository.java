package net.watchpeople.domain.box.repository;

import net.watchpeople.domain.box.entity.content.SharedBoxContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxContentRepository extends JpaRepository<SharedBoxContent, Long> {
}
