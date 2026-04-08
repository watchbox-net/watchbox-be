package net.watchbox.domain.content.repository;

import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    boolean existsByTmdbIdAndMediaType(Long tmdbId, MediaType mediaType);

    java.util.Optional<Content> findByTmdbIdAndMediaType(Long tmdbId, MediaType mediaType);
}
