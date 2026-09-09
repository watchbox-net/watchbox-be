package net.watchbox.domain.notification.entity;

/**
 * 알림 전달 채널.
 *
 * <p>지금은 계측 태그로만 쓰이지만, 채널마다 <b>만회 경로가 다르다</b>는 게 이후 설계의 축이 된다.
 * SSE 는 실패해도 알림함 + catchup 이 만회하지만, 메일은 못 보내면 그걸로 끝이다.
 * 그래서 재시도 정책도 채널마다 달라야 하고, 결국 전송 경로도 채널별로 갈라진다.
 */
public enum NotificationChannel {

    /** 브라우저 실시간 푸시. 탭이 열려 있을 때만 닿는다. */
    SSE,

    /** SES 메일. 앱 밖 채널이라 오프라인 사용자에게 닿는 유일한 경로다. */
    MAIL
}
