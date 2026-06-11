package net.watchbox.domain.box.service.history;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.history.BoxHistorySortOrder;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.history.BoxHistory;
import net.watchbox.domain.box.repository.history.BoxHistoryQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BoxHistoryQueryService {
    private final BoxHistoryQueryRepository boxHistoryQueryRepository;

    public List<BoxHistory> getBoxHistoryList(Box box, BoxHistorySortOrder sort, Long cursorId, int size) {
        return boxHistoryQueryRepository.findBoxHistoryList(box, sort, cursorId, size);
    }
}
