package net.watchbox.domain.content.dto.box;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentBoxUpdateResponse {
    private Long memberId;
    private Long contentId;
    private List<Long> addedBoxIds;
    private List<Long> removedBoxIds;
}
