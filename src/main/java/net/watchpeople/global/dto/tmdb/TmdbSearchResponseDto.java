package net.watchpeople.global.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// TMDB API 원본 응답 DTO
@Data
public class TmdbSearchResponseDto {
    private int page;

    @JsonProperty("total_results")
    private int totalResults;

    @JsonProperty("total_pages")
    private int totalPages;

    private List<TmdbResultDto> results;
}
