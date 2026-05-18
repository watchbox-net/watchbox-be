package net.watchbox.domain.record.dto.request;

public enum ContentRecordSortOrder {
    RECENT_UPDATED, OLDEST_UPDATED, // modifiedAt 기준
    RECENT_YEAR, OLDEST_YEAR // SubContent 연도 기준
    // 실제로는 연월일까지 정렬에 포함이지만 화면에 보여질건 연도뿐이라 YEAR로 네이밍
}
