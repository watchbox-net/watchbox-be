package net.watchpeople.domain.search.dto.response.list;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class SearchListResponse {
    private int page;
    private int totalResults;
    private int totalPages;
    private List<MultiSearchResponse> contentList;
}
