package net.watchbox.domain.content.sub.movie.repository;

import net.watchbox.domain.content.sub.movie.entity.MovieDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieDetailRepository extends JpaRepository<MovieDetail, Long> {
}
