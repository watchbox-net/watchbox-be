package net.watchbox.domain.content.common.dto.interaction;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.record.entity.WatchStatus;

@Getter
@ToString
@Builder
public class MemberInteraction {
    private Boolean isLiked;
    private WatchStatus watchStatus;
}
