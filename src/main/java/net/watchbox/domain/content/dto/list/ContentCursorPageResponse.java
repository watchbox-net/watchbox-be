package net.watchbox.domain.content.dto.list;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentCursorPageResponse {
    private List<ContentItem> contentItemList;
    private String nextCursor;
    private boolean hasNext;

    public static ContentCursorPageResponse empty() {
        return ContentCursorPageResponse.builder()
                .contentItemList(List.of())
                .nextCursor(null)
                .hasNext(false)
                .build();
    }
}
