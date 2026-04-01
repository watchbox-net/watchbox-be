package net.watchbox.domain.content.movie.repository;

import net.watchbox.domain.content.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Query("SELECT m FROM Movie m JOIN FETCH m.movieDetail WHERE m.tmdbId = :tmdbId")
    Optional<Movie> findWithDetailByTmdbId(@Param("tmdbId") Long tmdbId);

}
