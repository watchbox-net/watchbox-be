package net.watchbox.domain.content.common.dto.list;

import lombok.*;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentPageResponse {
    private List<ContentItem> contentItemList;
    private Long totalCount;
    private Long totalPages;
    private Long currentPage;

    public static ContentPageResponse empty() {
        return ContentPageResponse.builder()
                .contentItemList(List.of())
                .totalCount(0L)
                .totalPages(0L)
                .currentPage(0L)
                .build();
    }
}
