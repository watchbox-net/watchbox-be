package net.watchbox.domain.content.common.repository;

import net.watchbox.domain.content.common.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    @Modifying
    @Transactional
    @Query(
            value = "insert into content (tmdb_id, media_type, is_saved) values (:tmdbId, :mediaType, false) ",
            nativeQuery = true
    )
    void insertContent(Long tmdbId, String mediaType);
}
