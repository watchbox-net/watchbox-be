package net.watchbox.global.event;

/**
 * 이벤트가 나갈 <b>논리 토픽</b>. 실제 이름(환경 prefix 포함)은 {@code KafkaTopicProperties} 가 푼다.
 *
 * <p>토픽을 나누는 이유는 <b>타입이 섞이면 역직렬화가 깨지기</b> 때문이다.
 * 전역 consumer 설정이 타입 헤더를 쓰지 않아({@code use.type.headers=false}) 컨슈머가 대상 타입을
 * 고정해서 읽는데, 한 토픽에 종류가 다른 메시지가 섞이면 남의 메시지를 자기 타입으로 읽으려다 실패한다.
 */
public enum TopicKey {

    /** 전송 경로 헬스체크 프로브. */
    DOMAIN_EVENTS,

    /** 알림 봉투. 채널별 컨슈머 그룹이 각자 읽는다. */
    NOTIFICATION_EVENTS
}
