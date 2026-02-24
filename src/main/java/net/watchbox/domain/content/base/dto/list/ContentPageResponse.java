package net.watchbox.domain.content.base.dto.list;

import lombok.*;
import net.watchbox.domain.tmdb.response.movielists.TmdbMovieListsResponse;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentPageResponse {
    private List<ContentItem> contentItemList;
    private int totalCount;
    private int totalPages;
    private int currentPage;

    public static ContentPageResponse empty() {
        return ContentPageResponse.builder()
                .contentItemList(List.of())
                .totalCount(0)
                .totalPages(0)
                .currentPage(0)
                .build();
    }
}
