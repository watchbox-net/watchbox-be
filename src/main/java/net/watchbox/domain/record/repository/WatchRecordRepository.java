package net.watchbox.domain.record.repository;

import net.watchbox.domain.record.entity.WatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WatchRecordRepository extends JpaRepository<WatchRecord, Long> {

}
