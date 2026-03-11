package net.watchbox.domain.content.base.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@Builder
public final class MovieDetailResponse implements ContentDetail {
    private Long tmdbId;
    private String titleKo;
    private String titleOriginal;
    private String posterPath;
    private Integer year;

    private List<String> genreList; // 변환

    private String overview;
    private String backdropPath;
    private String originalLanguage;
    private LocalDate releaseDate;
    private Boolean adult;
    private Boolean video;

    private String status;
    private Integer runtime;
    private String tagline;
    private String homepage;
    private Long budget;
    private Long revenue;

    // 변환
    private List<String> originCountryList; // "['US', 'KR']"
}
