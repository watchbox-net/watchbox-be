package net.watchbox.domain.content.sub.tv.repository;

import net.watchbox.domain.content.sub.tv.entity.Tv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TvRepository extends JpaRepository<Tv, Long> {
    Optional<Tv> findByTmdbId(Long tmdbId);

    @Query("SELECT t FROM Tv t JOIN FETCH t.tvDetail WHERE t.tmdbId = :tmdbId")
    Optional<Tv> findWithDetailByTmdbId(@Param("tmdbId") Long tmdbId);

}
