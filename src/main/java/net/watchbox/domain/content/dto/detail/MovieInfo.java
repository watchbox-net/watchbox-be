package net.watchbox.domain.content.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.dto.detail.credit.person.PersonCredit;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@Builder
public final class MovieInfo implements ContentInfo { // 상세 페이지 순서대로 필드 나열
    private Long contentId;
    private Long tmdbId;

    private String backdropPath;        // 배경 이미지
    private String posterPath;          // 포스터
    private String titleKo;             // 제목
    private String titleOriginal;       // 원제
    private Integer year;               // 연도
    private List<String> genreList;     // 장르 리스트
    private Integer runtime;            // 러닝타임
    private String overview;            // 줄거리

    private LocalDate releaseDate;              // 개봉
    private String originCountry;               // 국가
//    private List<String> productionCompanyList;     // 제작사 : 보류

    // --------- append_to_response ---------
    private List<String> watchProviderList;     // 플랫폼 리스트
    private PersonCredit personCredit;          // 출연진, 제작진
    private List<String> backdropPathList;      // 배경 이미지 리스트
    // videos

    // --------- 미사용 ---------
    private String originalLanguage;
    private Boolean adult;
    private Boolean video;
    private String status;
    private String tagline;
    private String homepage;
    private Long budget;
    private Long revenue;
//    private List<String> productionCountryList; // "['US', 'KR']"


}
