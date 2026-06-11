package net.watchbox.domain.record.service.history;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.history.ContentRecordHistoryQueryRequest;
import net.watchbox.domain.record.entity.history.ContentRecordHistory;
import net.watchbox.domain.record.repository.history.ContentRecordHistoryQueryRepository;
import net.watchbox.global.dto.CursorPayload;
import net.watchbox.global.util.CursorCodec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ContentRecordHistoryQueryService {
    private final ContentRecordHistoryQueryRepository contentRecordHistoryQueryRepository;
    private final CursorCodec cursorCodec;

    public List<ContentRecordHistory> getMyHistoryList(Member member, ContentRecordHistoryQueryRequest request, int size) {
        CursorPayload cursor = cursorCodec.decode(request.getCursor()); // null 이면 첫 페이지
        return contentRecordHistoryQueryRepository.findMyHistoryList(member, request, cursor, size);
    }
}
