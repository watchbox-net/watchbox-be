package net.watchbox.domain.box.dto.box.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.dto.box.BoxItem;

import java.util.List;

@Getter
@ToString
@Builder
public class BoxPageResponse {
    private List<BoxItem> boxItemList;
    private Long totalCount;
}
