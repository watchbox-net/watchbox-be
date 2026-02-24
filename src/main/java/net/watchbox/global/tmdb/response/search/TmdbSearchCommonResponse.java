package net.watchbox.global.tmdb.response.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.search.TmdbSearchResultItem;

import java.util.List;

// TMDB API 원본 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbSearchCommonResponse {
    private int page;

    @JsonProperty("total_results")
    private int totalResults;

    @JsonProperty("total_pages")
    private int totalPages;

    private List<TmdbSearchResultItem> results;
}
