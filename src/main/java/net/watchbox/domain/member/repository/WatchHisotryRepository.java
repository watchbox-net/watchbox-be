package net.watchbox.domain.member.repository;

import net.watchbox.domain.member.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WatchHisotryRepository extends JpaRepository<WatchHistory, Long> {

}
