package net.watchpeople.global.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
public final class ConvertUtils {
    /**
     * 날짜 문자열을 LocalDate로 변환
     */
    public static LocalDate parseReleaseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {}", dateString);
            return null;
        }
    }
}
