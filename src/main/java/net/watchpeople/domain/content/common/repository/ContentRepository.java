package net.watchpeople.domain.content.common.repository;

import net.watchpeople.domain.content.common.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
}
