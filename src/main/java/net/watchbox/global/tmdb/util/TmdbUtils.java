package net.watchbox.global.tmdb.util;

import java.time.LocalDate;

public final class TmdbUtils {
    public static final int TMDB_MAX_RETRY = 3;
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

    public static Integer calculateAge(String birthday, String deathday) {
        if (birthday == null || birthday.isBlank()) return null;
        LocalDate birthDate = LocalDate.parse(birthday);
        LocalDate endDate = (deathday != null && !deathday.isBlank()) ? LocalDate.parse(deathday) : LocalDate.now();
        return endDate.getYear() - birthDate.getYear() - (endDate.getDayOfYear() < birthDate.getDayOfYear() ? 1 : 0);
    }
}
