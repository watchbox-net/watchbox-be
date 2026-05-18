package net.watchbox.domain.record.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentRecordCountResponse {
    private long totalCount;

    public static ContentRecordCountResponse of(long totalCount) {
        return ContentRecordCountResponse.builder()
                .totalCount(totalCount)
                .build();
    }
}
