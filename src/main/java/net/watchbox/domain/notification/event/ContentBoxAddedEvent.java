package net.watchbox.domain.notification.event;

import net.watchbox.domain.notification.dto.payload.ContentBoxAddedPayload;
import net.watchbox.domain.notification.entity.NotificationType;

import java.util.List;

/**
 * 박스에 컨텐츠 추가 시 발행.
 * 수신자 = 박스 멤버 중 추가자(publisher)를 제외한 나머지.
 *
 * <p>공유 박스든 마이 박스든 상관없이 사용 — boxType 정보는 payload 안에 들어있음.
 * 마이 박스의 경우 receiverIds 가 비어있을 수 있음 (본인 외 멤버 없으므로 별도 발행 안 함이 일반적).
 *
 * <p>발행 위치 예: BoxContentFacade.addBoxContent(...) 트랜잭션 안.
 */
public record ContentBoxAddedEvent(
        List<Long> receiverIds,
        ContentBoxAddedPayload payload
) implements NotificationEvent {

    @Override
    public NotificationType type() {
        return NotificationType.BOX_CONTENT_ADDED;
    }
}
