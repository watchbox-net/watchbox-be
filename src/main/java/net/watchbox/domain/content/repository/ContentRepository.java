package net.watchbox.domain.content.repository;

import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findAllByTmdbIdIn(List<Long> tmdbIds);

    boolean existsByTmdbIdAndMediaType(Long tmdbId, MediaType mediaType);
}
