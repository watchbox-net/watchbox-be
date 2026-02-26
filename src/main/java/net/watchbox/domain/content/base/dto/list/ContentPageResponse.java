package net.watchbox.domain.content.base.dto.list;

import lombok.*;
import net.watchbox.domain.content.base.dto.meta.ResponseMeta;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentPageResponse {
    private List<ContentItem> contentItemList;
    private int totalCount;
    private int totalPages;
    private int currentPage;
    private ResponseMeta responseMeta;

    public static ContentPageResponse empty() {
        return ContentPageResponse.builder()
                .contentItemList(List.of())
                .totalCount(0)
                .totalPages(0)
                .currentPage(0)
                .build();
    }
}
