package net.watchbox.domain.content.repository;

import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByTmdbIdAndMediaType(Long tmdbId, MediaType mediaType);

    boolean existsByTmdbIdAndMediaType(Long tmdbId, MediaType mediaType);
}
