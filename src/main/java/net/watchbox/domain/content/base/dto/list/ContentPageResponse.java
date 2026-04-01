package net.watchbox.domain.content.base.dto.list;

import lombok.*;
import net.watchbox.domain.content.base.dto.meta.ResponseMeta;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentPageResponse {
    private List<ContentItem> contentItemList;
    private Long totalCount;
    private Long totalPages;
    private Long currentPage;
    private ResponseMeta responseMeta; // 아직 미사용중

    public static ContentPageResponse empty() {
        return ContentPageResponse.builder()
                .contentItemList(List.of())
                .totalCount(0L)
                .totalPages(0L)
                .currentPage(0L)
                .build();
    }
}
