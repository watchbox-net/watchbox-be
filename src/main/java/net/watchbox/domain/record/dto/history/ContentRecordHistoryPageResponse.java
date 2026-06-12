package net.watchbox.domain.record.dto.history;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentRecordHistoryPageResponse {
    private List<ContentRecordHistoryItem> historyList;
    private String nextCursor;
    private boolean hasNext;

    public static ContentRecordHistoryPageResponse empty() {
        return ContentRecordHistoryPageResponse.builder()
                .historyList(List.of())
                .nextCursor(null)
                .hasNext(false)
                .build();
    }
}
