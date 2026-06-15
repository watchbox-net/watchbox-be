package net.watchbox.domain.record.dto.record.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentRecordCountResponse {
    private Long totalCount;

    public static ContentRecordCountResponse of(Long totalCount) {
        return ContentRecordCountResponse.builder()
                .totalCount(totalCount)
                .build();
    }
}
