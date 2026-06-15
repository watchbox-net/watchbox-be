package net.watchbox.domain.box.dto.history;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class BoxHistoryPageResponse {
    private List<BoxHistoryItem> historyList;
    private Long nextCursor; // 다음 페이지 cursorId (마지막 boxHistoryId). 없으면 null
    private boolean hasNext;

    public static BoxHistoryPageResponse empty() {
        return BoxHistoryPageResponse.builder()
                .historyList(List.of())
                .nextCursor(null)
                .hasNext(false)
                .build();
    }
}
