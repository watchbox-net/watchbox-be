package net.watchbox.domain.box.service.history;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.history.BoxHistory;
import net.watchbox.domain.box.entity.history.BoxHistoryEventType;
import net.watchbox.domain.box.repository.history.BoxHistoryRepository;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BoxHistoryCommandService {
    private final BoxHistoryRepository boxHistoryRepository;

    /** 콘텐츠 추가 기록. */
    public void contentAdded(Box box, Member actor, Content content) {
        boxHistoryRepository.save(
                BoxHistory.ofContentEvent(box, actor, BoxHistoryEventType.CONTENT_ADDED, content));
    }

    /** 콘텐츠 삭제 기록. */
    public void contentDeleted(Box box, Member actor, Content content) {
        boxHistoryRepository.save(
                BoxHistory.ofContentEvent(box, actor, BoxHistoryEventType.CONTENT_DELETED, content));
    }

    /** 멤버 합류 기록 (초대 수락). */
    public void memberJoined(Box box, Member actor, Member targetMember) {
        boxHistoryRepository.save(
                BoxHistory.ofMemberEvent(box, actor, BoxHistoryEventType.MEMBER_JOINED, targetMember));
    }
}
