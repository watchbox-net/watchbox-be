package net.watchbox.domain.content.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.dto.detail.credit.person.AggregatePersonCredit;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@Builder
public final class TvInfo implements ContentInfo {
    private Long contentId;
    private Long tmdbId;

    private String backdropPath;        // 배경 이미지
    private String posterPath;          // 포스터
    private String nameKo;              // 제목
    private String nameOriginal;        // 원제
    private Integer firstYear;          // 시작 연도
    private Integer lastYear;           // 종료 연도
    private List<String> genreList;     // 장르 리스트
    private String overview;            // 줄거리

    private LocalDate firstAirDate;             // 첫 방영일
    private LocalDate lastAirDate;              // 마지막 방영일
    private Integer numberOfSeasons;               // 시즌 수
    private String originCountry;               // 국가
//    private List<String> productionCompanyList;     // 제작사 : 보류

    // --------- append_to_response ---------
    private List<String> watchProviderList;     // 플랫폼 리스트
    private AggregatePersonCredit personCredit;       // 출연진, 제작진 (시즌별 누적)
    private List<String> backdropPathList;      // 배경 이미지 리스트
    // videos

    // --------- 미사용 ---------
    private Double popularity;
    private Boolean inProduction; // 현재 제작/방영중
    private String status;
    private String tagline;
//    private String originalLanguage;
//    private String type; // TV 프로그램 유형
//    private String homepage;
//    private Long budget;  // 제작비 (USD)
//    private Long revenue;  // 수익 (USD)
//    private Integer numberOfEpisodes;

    // 변환
//    private List<String> productionCountryList; // "['US', 'KR']"
}
