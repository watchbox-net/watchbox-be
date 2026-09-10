package net.watchbox.domain.notification.channel;

/**
 * 다시 시도해도 결과가 같은 실패.
 *
 * <p>예: 발송 가능한 메일 주소가 없다. 몇 번을 보내도 없는 건 없다.
 * 이런 건 재시도 상한을 기다릴 이유가 없어 바로 종결한다 —
 * 계속 붙들고 있으면 <b>배치를 막고, 성공한 채널까지 반복 실행</b>시킨다.
 */
public class NonRetryableDeliveryException extends RuntimeException {

    public NonRetryableDeliveryException(String message) {
        super(message);
    }
}
