package net.watchbox.domain.record.service.history;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.record.repository.history.ContentRecordHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ContentRecordHistoryQueryService {
    private final ContentRecordHistoryRepository contentRecordHistoryRepository;
}
