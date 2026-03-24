package net.watchbox.global.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
public final class ConvertUtils {  //
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

    /**
     * 평점 소수점 첫째 자리까지 반올림
     */
    public static Double roundVoteAverage(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10) / 10.0;
    }
}
