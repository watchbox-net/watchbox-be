package net.watchbox.domain.content.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@Builder
public final class PersonInfo implements ContentInfo {
    private Long contentId;
    private String nameKo;
    private String nameEn;
    private String nameOriginal;
    private String profilePath;
    private String knownForDepartment;
    private Double popularity;

    private List<String> knownForList; // 변환 -> 작품 제목

    private String biography;
    private Integer gender;
    private LocalDate birthday;
    private LocalDate deathday;
    private String placeOfBirth;
    private String homepage;
    private Boolean adult;
}
