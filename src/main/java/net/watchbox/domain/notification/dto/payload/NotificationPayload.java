package net.watchbox.domain.notification.dto.payload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.watchbox.domain.notification.entity.NotificationType;

/**
 * 알림 타입별 부가 정보 (스냅샷).
 *
 * <p>구현 노트
 * <ul>
 *   <li>Jackson JsonTypeInfo + JsonSubTypes 로 type discriminator 기반 직렬화/역직렬화</li>
 *   <li>JSON 안의 "type" 필드가 NotificationType enum name 과 일치해야 함</li>
 *   <li>새 타입 추가 시: 이 sealed interface 의 permits + JsonSubTypes 매핑 + NotificationType enum 동시 갱신</li>
 *   <li>참조하는 엔티티(Box, Member 등)는 ID + 표시용 스냅샷(닉네임, 이름) 만 담음
 *       → 원본이 삭제/변경돼도 알림은 발송 시점 그대로 보존</li>
 * </ul>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BoxInvitationPayload.class, name = "BOX_INVITATION_RECEIVED"),
        @JsonSubTypes.Type(value = BoxInvitationRespondedPayload.class, name = "BOX_INVITATION_RESPONDED"),
        @JsonSubTypes.Type(value = BoxContentAddedPayload.class, name = "BOX_CONTENT_ADDED")
})
public sealed interface NotificationPayload
        permits BoxInvitationPayload, BoxInvitationRespondedPayload, BoxContentAddedPayload {

    /** 페이로드의 알림 타입 (NotificationType enum 과 1:1). */
    NotificationType type();
}
