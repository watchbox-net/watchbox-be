package net.watchbox.domain.content.sub.movie.repository;

import net.watchbox.domain.content.sub.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

}
