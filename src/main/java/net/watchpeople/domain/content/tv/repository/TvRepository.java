package net.watchpeople.domain.content.tv.repository;

import net.watchpeople.domain.content.tv.entity.Tv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TvRepository extends JpaRepository<Tv, Long> {
}
