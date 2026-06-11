package net.watchbox.domain.record.service.history;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.history.ContentRecordHistory;
import net.watchbox.domain.record.entity.history.ContentRecordHistoryEventType;
import net.watchbox.domain.record.entity.record.WatchStatus;
import net.watchbox.domain.record.repository.history.ContentRecordHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class ContentRecordHistoryCommandService {
    private final ContentRecordHistoryRepository contentRecordHistoryRepository;

    /** 시청 상태 변경 기록 (old → new). */
    public void watchStatusChange(Member member, Content content,
                                  WatchStatus oldStatus, WatchStatus newStatus) {
        contentRecordHistoryRepository.save(
                ContentRecordHistory.ofStatusChange(member, content, oldStatus, newStatus));
    }

    /** 좋아요 등록 기록. */
    public void likeAdded(Member member, Content content) {
        contentRecordHistoryRepository.save(
                ContentRecordHistory.ofLike(member, content, ContentRecordHistoryEventType.LIKE_ADDED));
    }

    /** 좋아요 삭제 기록. */
    public void likeRemoved(Member member, Content content) {
        contentRecordHistoryRepository.save(
                ContentRecordHistory.ofLike(member, content, ContentRecordHistoryEventType.LIKE_REMOVED));
    }
}
