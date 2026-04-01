package net.watchbox.global.tmdb.util;

import java.time.LocalDate;

public final class TmdbUtils {
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w500";

    public static Integer extractYear(String date) {
        if (date == null || date.isBlank()) return null;
        return Integer.parseInt(date.substring(0, 4));
//        return LocalDate.parse(date).getYear();
    }

    public static LocalDate extractDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date);
    }

    /**
     * 이미지 전체 URL 생성
     */
    public static String buildImageFullUrl(String path) {
        return path != null ? IMAGE_URL + path : null;
    }

}
