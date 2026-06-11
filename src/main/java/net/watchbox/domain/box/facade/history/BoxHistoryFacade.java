package net.watchbox.domain.box.facade.history;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.history.BoxHistoryItem;
import net.watchbox.domain.box.dto.history.BoxHistoryPageResponse;
import net.watchbox.domain.box.dto.history.BoxHistorySortOrder;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.history.BoxHistory;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.history.BoxHistoryQueryService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.content.mapper.box.BoxHistoryMapper;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Observed
public class BoxHistoryFacade {
    private final BoxService boxService;
    private final BoxValidator boxValidator;
    private final BoxHistoryQueryService boxHistoryQueryService;

    @Transactional(readOnly = true)
    public BoxHistoryPageResponse getBoxHistoryPage(Member member, Long boxId, BoxHistorySortOrder sort, Long cursorId, int size) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        boxValidator.validateBoxMember(box, member); // 박스 멤버만 히스토리 조회 가능

        // size+1 개를 받음 - hasNext 판단용
        List<BoxHistory> fetched = boxHistoryQueryService.getBoxHistoryList(box, sort, cursorId, size);

        boolean hasNext = fetched.size() > size;
        List<BoxHistory> page = hasNext ? fetched.subList(0, size) : fetched;

        Long nextCursor = hasNext ? page.getLast().getBoxHistoryId() : null;
        List<BoxHistoryItem> historyList = BoxHistoryMapper.toItems(page);

        return BoxHistoryPageResponse.builder()
                .historyList(historyList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
