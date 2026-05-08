package net.watchbox.global.dto.request;

public enum SortOrder {
    RECENT_SAVED, OLDEST_SAVED,
    RECENT_YEAR, OLDEST_YEAR
    // 실제로는 연월일까지 정렬에 포함이지만 화면에 보여질건 연도뿐이라 YEAR로 네이밍
}
