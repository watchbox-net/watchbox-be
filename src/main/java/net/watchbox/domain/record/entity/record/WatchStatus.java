package net.watchbox.domain.record.entity.record;

public enum WatchStatus {
    COMPLETED,
    WATCHING,
    PLANNED,
    PAUSED,
    NONE // NONE은 시청 기록이 없는 상태 (DTO 반환용, DB에는 저장되지 않는 타입) | 현재 미사용중
    // DROP = null
}

