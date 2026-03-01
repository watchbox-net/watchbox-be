package net.watchbox.global.dev.deprecated.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;

import java.util.List;

@Getter
@ToString
@Builder
public class TvListResponse {
    private Integer page;
    private Integer totalPages;
    private Integer totalResults;
    private List<TvResponse> tvList;

    public static TvListResponse from(TmdbTvSeriesListsResponse response) {
        List<TvResponse> tvList = response.getResults().stream()
                .map(TvResponse::from)
                .toList();

        return TvListResponse.builder()
                .page(response.getPage())
                .totalPages(response.getTotalPages())
                .totalResults(response.getTotalResults())
                .tvList(tvList)
                .build();
    }
}
