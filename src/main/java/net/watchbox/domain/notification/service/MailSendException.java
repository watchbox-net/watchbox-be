package net.watchbox.domain.notification.service;

/**
 * 메일 발송 실패. <b>재시도할 수 있는 실패</b>라는 뜻으로 던진다.
 *
 * <p>예전에는 발송 실패를 로그만 남기고 삼켰다. 호출자가 이미 커밋된 작업의 후처리라
 * 실패를 알아도 할 수 있는 게 없었기 때문이다. outbox 도입 후에는 릴레이가 이 예외를 받아
 * 행을 미발행으로 남기고 재시도하므로, <b>삼키면 그 재시도가 통째로 사라진다.</b>
 */
public class MailSendException extends RuntimeException {

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
