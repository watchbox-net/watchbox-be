package net.watchbox.domain.box.dto.box;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class BoxPageResponse {
    private List<BoxItem> boxItemList;
    private Long totalCount;
}
