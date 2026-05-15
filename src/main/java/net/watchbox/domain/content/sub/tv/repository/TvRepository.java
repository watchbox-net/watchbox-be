package net.watchbox.domain.content.sub.tv.repository;

import net.watchbox.domain.content.sub.tv.entity.Tv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TvRepository extends JpaRepository<Tv, Long> {

}
