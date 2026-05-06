package net.watchbox.domain.content.sub.person.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
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
    COSTUME_MAKE_UP("Costume & Make-Up", "의상");

    private final String englishValue;
    private final String koreanValue;

    private static final Map<String, Department> BY_ENGLISH_VALUE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Department::getEnglishValue, d -> d));

    @JsonValue
    public String toJson() {
        return englishValue;
    }

    @JsonCreator
    public static Department fromEnglishValue(String englishValue) {
        if (englishValue == null) {
            return null;
        }
        Department d = BY_ENGLISH_VALUE.get(englishValue);
        if (d == null) {
            throw new IllegalArgumentException("Unknown Department: " + englishValue);
        }
        return d;
    }

    public static String getKoreanByEnglishValue(String englishValue) {
        Department d = BY_ENGLISH_VALUE.get(englishValue);
        return d != null ? d.koreanValue : null;
    }
}
