package net.watchbox.domain.box.repository.history;

import net.watchbox.domain.box.entity.history.BoxHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxHistoryRepository extends JpaRepository<BoxHistory, Integer> {
}
