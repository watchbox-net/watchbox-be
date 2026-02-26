package net.watchbox.global.tmdb.util;

public final class TmdbUtils {
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w500";

    public static int extractYear(String dateString) {
        return Integer.parseInt(dateString.substring(0, 4));
    }

    /**
     * 이미지 전체 URL 생성
     */
    public static String buildImageFullUrl(String path) {
        return path != null ? IMAGE_URL + path : null;
    }

}
