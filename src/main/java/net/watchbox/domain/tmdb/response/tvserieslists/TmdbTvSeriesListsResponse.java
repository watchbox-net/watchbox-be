package net.watchbox.domain.tmdb.response.tvserieslists;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbTvSeriesListsResponse {
    private Integer page;

    private List<TmdbTvSeriesListsResultItem> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;
}
