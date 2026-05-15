package net.watchbox.global.tmdb.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Slf4j
public enum Department {
    ACTING("Acting", "배우"),
    DIRECTING("Directing", "감독"),
    WRITING("Writing", "각본"),
    PRODUCTION("Production", "제작"),
    SOUND("Sound", "음악"),
    VISUAL_EFFECTS("Visual Effects", "시각 효과"),
    ART("Art", "미술"),
    EDITING("Editing", "편집"),
    CAMERA("Camera", "촬영"),
    LIGHTING("Lighting", "조명"),
    CREW("Crew", "스태프"),
    COSTUME_MAKE_UP("Costume & Make-Up", "의상"),
    UNKNOWN("unknown", "미확인"); // 확인해서 추가해야함

    private final String englishValue;
    private final String koreanValue;

    private static final Map<String, Department> BY_ENGLISH_VALUE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Department::getEnglishValue, d -> d));

    // JSON 직렬화 시 enum 상수명(ACTING) 대신 koreanValue("배우")로 응답에 나가게 함
    @JsonValue
    public String toJson() {
        return koreanValue;
    }

    // JSON 역직렬화 시 TMDB가 보낸 englishValue("Acting")를 enum 상수(ACTING)로 변환
    @JsonCreator
    public static Department fromEnglishValue(String englishValue) {
        if (englishValue == null) {
            return null;
        }
        Department d = BY_ENGLISH_VALUE.get(englishValue);
        if (d == null) {
            log.info("확인이 필요한 새로운 department {}", englishValue);
            return UNKNOWN;
//            throw new IllegalArgumentException("Unknown Department: " + englishValue);
        }
        return d;
    }

    public static String getKoreanByEnglishValue(String englishValue) {
        Department d = BY_ENGLISH_VALUE.get(englishValue);
        return d != null ? d.koreanValue : null;
    }
}
