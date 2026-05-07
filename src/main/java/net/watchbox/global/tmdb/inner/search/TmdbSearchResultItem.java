package net.watchbox.global.tmdb.inner.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// TMDB API 원본 결과 DTO
@Data
public class TmdbSearchResultItem {
    private Long id;
    private String title; // 영화
    private String name; // TV | 인물

    @JsonProperty("original_title")
    private String originalTitle; // 영화

    @JsonProperty("original_name")
    private String originalName; // TV | 인물

    private String overview;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    @JsonProperty("poster_path")
    private String posterPath; // 영화 | TV

    @JsonProperty("profile_path")
    private String profilePath; // 인물

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("first_air_date")
    private String firstAirDate; // TV

    @JsonProperty("last_air_date")
    private String lastAirDate; // TV

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Long voteCount;

    private Double popularity;

    @JsonProperty("media_type")
    private String mediaType;

    @JsonProperty("original_language")
    private String originalLanguage;
}
