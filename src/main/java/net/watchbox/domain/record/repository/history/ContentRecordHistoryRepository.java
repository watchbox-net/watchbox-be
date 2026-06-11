package net.watchbox.domain.record.repository.history;

import net.watchbox.domain.record.entity.history.ContentRecordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRecordHistoryRepository extends JpaRepository<ContentRecordHistory, Long> {
}
