package net.watchbox.domain.tmdb.util;

public final class TmdbUtils {
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w500";


    /**
     * 이미지 전체 URL 생성
     */
    public static String buildImageFullUrl(String path) {
        return path != null ? IMAGE_URL + path : null;
    }

}
