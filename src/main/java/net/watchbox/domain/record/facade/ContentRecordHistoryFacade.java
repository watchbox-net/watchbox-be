package net.watchbox.domain.record.facade;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.mapper.record.ContentRecordHistoryMapper;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.history.ContentRecordHistoryItem;
import net.watchbox.domain.record.dto.history.ContentRecordHistoryPageResponse;
import net.watchbox.domain.record.dto.history.ContentRecordHistoryQueryRequest;
import net.watchbox.domain.record.entity.history.ContentRecordHistory;
import net.watchbox.domain.record.service.history.ContentRecordHistoryQueryService;
import net.watchbox.global.constants.AppConstants;
import net.watchbox.global.dto.CursorPayload;
import net.watchbox.global.util.CursorCodec;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Observed
public class ContentRecordHistoryFacade {
    private final ContentRecordHistoryQueryService contentRecordHistoryQueryService;
    private final CursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public ContentRecordHistoryPageResponse getMyContentRecordHistoryPage(Member member, ContentRecordHistoryQueryRequest request) {
        // HISTORY_PAGE_SIZE+1 개를 받음 - hasNext 판단용
        List<ContentRecordHistory> fetched = contentRecordHistoryQueryService.getMyHistoryList(
                member, request, AppConstants.HISTORY_PAGE_SIZE);

        boolean hasNext = fetched.size() > AppConstants.HISTORY_PAGE_SIZE;
        List<ContentRecordHistory> page = hasNext ? fetched.subList(0, AppConstants.HISTORY_PAGE_SIZE) : fetched;

        String nextCursor = hasNext
                ? cursorCodec.encode(buildCursor(page.getLast())) // = get(page.size()-1))
                : null;

        List<ContentRecordHistoryItem> historyList = ContentRecordHistoryMapper.toItems(page);

        return ContentRecordHistoryPageResponse.builder()
                .historyList(historyList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    // 정렬 키가 createdAt + id 단일 차원이라 CursorPayload 를 직접 구성
    private CursorPayload buildCursor(ContentRecordHistory last) {
        return CursorPayload.of(last.getCreatedAt(), last.getContentRecordHistoryId());
    }
}
