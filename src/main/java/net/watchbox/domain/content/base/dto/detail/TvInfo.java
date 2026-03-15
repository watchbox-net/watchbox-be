package net.watchbox.domain.content.base.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@Builder
public final class TvInfo implements ContentInfo {
    private Long contentId;
    private String nameKo;
    private String nameOriginal;
    private String posterPath;
    private Double popularity;
    private Integer year;

    private List<String> genreList; // 변환

    private String overview;
    private String backdropPath;
    private String originalLanguage;
    private LocalDate firstAirDate;
    private Boolean adult;
    private Boolean video;

    private String status;
    private String type; // TV 프로그램 유형
    private String tagline;
    private String homepage;
    private Long budget;  // 제작비 (USD)
    private Long revenue;  // 수익 (USD)
    private Integer numberOfEpisodes;
    private Integer numberOfSeasons;
    private LocalDate lastAirDate;

    // 변환
    private List<String> languageList;  // "['en', 'ko']"
    private List<String> originCountryList;  // "['US', 'KR']"
}
