package net.watchbox.domain.box.dto.content;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoxContentCountResponse {
    private Long totalCount;

    public static BoxContentCountResponse of(Long totalCount) {
        return BoxContentCountResponse.builder()
                .totalCount(totalCount)
                .build();
    }
}
