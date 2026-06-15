package net.watchbox.domain.box.dto.history;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.history.BoxHistoryEventType;
import net.watchbox.domain.content.dto.list.ContentSummary;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
public class BoxHistoryItem {
    private Long boxHistoryId;
    private BoxHistoryEventType eventType;
    private BoxHistoryMemberSummary actor;        // 행위 주체 (탈퇴 시 null)
    private BoxHistoryMemberSummary targetMember; // MEMBER_* 이벤트 대상 (그 외 null)
    private ContentSummary content;               // CONTENT_* 이벤트 대상 (그 외 null)
    private String oldValue;                      // XX_CHANGED 이벤트 변경 전 값
    private String newValue;                      // XX_CHANGED 이벤트 변경 후 값
    private LocalDateTime createdAt;
}
