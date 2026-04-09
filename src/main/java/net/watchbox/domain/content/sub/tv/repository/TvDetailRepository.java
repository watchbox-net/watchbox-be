package net.watchbox.domain.content.sub.tv.repository;

import net.watchbox.domain.content.sub.tv.entity.TvDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TvDetailRepository extends JpaRepository<TvDetail, Long> {
}
