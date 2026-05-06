package net.watchbox.domain.content.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.dto.detail.credit.work.WorkCredit;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@Builder
public final class PersonInfo implements ContentInfo {
    private Long contentId;
    private Long tmdbId;

    private String profilePath;         // 프로필 이미지
    private String nameKo;              // 한글 이름
    private String nameEn;              // 영문 이름
    private String nameOriginal;        // 원래 이름
    private LocalDate birthday;         // 생년월일
    private Integer age;                // 나이 (생년월일로 계산)
    private String placeOfBirth;        // 출생지
    private String knownForDepartment;  // 주요 활동 분야 (Acting, Directing 등)

    private WorkCredit credit;    // 작품 정보

    private String biography;

    // --------- 미사용 ---------
    private Double popularity;
    private List<String> knownForList; // 변환 -> 작품 제목
    private Integer gender;
    private LocalDate deathday;
    private String homepage;
    private Boolean adult;
}
