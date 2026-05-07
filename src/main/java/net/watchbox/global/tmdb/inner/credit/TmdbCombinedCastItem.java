package net.watchbox.global.tmdb.inner.credit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Person combined_credits 의 cast 항목.
 * 영화/TV 가 한 배열에 섞여서 옴 -> media_type 으로 구별.
 *
 * - mediaType = "movie" 일 때는 title/originalTitle/releaseDate/video 사용
 * - mediaType = "tv"    일 때는 name/originalName/firstAirDate/originCountry/episodeCount/firstCreditAirDate 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCombinedCastItem {

    /** "movie" 또는 "tv" */
    @JsonProperty("media_type")
    private String mediaType;

    // ===== 공통 =====
    private boolean adult;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    private Long id;

    @JsonProperty("original_language")
    private String originalLanguage;

    private String overview;

    private Double popularity;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Long voteCount;

    // ===== movie 전용 =====
    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("release_date")
    private String releaseDate;

    private boolean video;

    // ===== tv 전용 =====
    private String name;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("origin_country")
    private List<String> originCountry;

    @JsonProperty("episode_count")
    private Integer episodeCount;

    @JsonProperty("first_credit_air_date")
    private String firstCreditAirDate;

    // ===== 출연 정보 =====
    private String character;

    @JsonProperty("credit_id")
    private String creditId;

    private Integer order;

    // ===== Helper =====
    public boolean isMovie() {
        return "movie".equals(mediaType);
    }

    public boolean isTv() {
        return "tv".equals(mediaType);
    }

    /** 영화면 title, TV면 name 반환. */
    public String getDisplayTitle() {
        return isMovie() ? title : name;
    }

    /** 영화면 release_date, TV면 first_air_date 반환. */
    public String getDisplayDate() {
        return isMovie() ? releaseDate : firstAirDate;
    }
}
