package net.watchbox.domain.content.dto.box;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentBoxSheetResponse {
    List<ContentBoxItem> contentBoxItemList;
    private Long totalCount;
}
