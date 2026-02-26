package net.watchbox.domain.content.base.dto.interaction;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.record.entity.WatchStatus;

@Getter
@ToString
@Builder
public class MemberInteraction {
    private Boolean liked;
    private WatchStatus watchStatus;
}
