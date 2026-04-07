package net.watchbox.domain.content.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.entity.MediaType;

@Getter
@ToString
@Builder
public class ContentDetailResponse {
    private MediaType mediaType;
    private ContentInfo contentInfo;
    private MemberRecord memberRecord;
}
