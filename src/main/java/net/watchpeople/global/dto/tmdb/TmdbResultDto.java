package net.watchpeople.global.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// TMDB API 원본 결과 DTO
@Data
public class TmdbResultDto {
    private Long id;
    private String title;
    private String name; // TV 프로그램 | 배우 일때

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("original_name")
    private String originalName; // TV 프로그램 | 배우 일때

    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("first_air_date")
    private String firstAirDate; // TV 프로그램 | 배우 일때

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    private Double popularity;

    @JsonProperty("media_type")
    private String mediaType;

    @JsonProperty("original_language")
    private String originalLanguage;
}
