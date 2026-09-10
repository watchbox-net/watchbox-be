package net.watchbox.domain.notification.delivery;

/** 채널 단위 전달 상태. */
public enum DeliveryStatus {

    /** 시도했으나 실패. 재시도 대기 중이다. */
    PENDING,

    /** 전달 완료. 다시 보내면 안 된다. */
    SENT,

    /** 종결된 실패. 재시도 상한에 걸렸거나 재시도해도 소용없는 실패다. 사람이 봐야 한다. */
    FAILED;

    /** 더 이상 시도하지 않아도 되는 상태. */
    public boolean isTerminal() {
        return this == SENT || this == FAILED;
    }
}
