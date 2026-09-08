package net.watchbox.global.constants;

/**
 * 앱 전역에서 공유하는 상수 모음.
 * 카테고리별 섹션 주석으로 그룹핑. 늘어나면 inner static class 로 분리.
 */
public final class AppConstants {

    private AppConstants() {}

    // ============== Pagination ==============

    /** 무한스크롤 리스트 페이지 크기 (시청 기록, 박스 콘텐츠 등) */
    public static final int PAGE_SIZE = 20; // 페이지 리스트 응답 Item 개수 단위

    /** 시청 기록 히스토리 무한스크롤 페이지 크기 */
    public static final int HISTORY_PAGE_SIZE = 30;
}
