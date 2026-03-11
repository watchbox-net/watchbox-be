package net.watchbox.domain.content.base.dto.detail;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.base.dto.interaction.MemberInteraction;
import net.watchbox.domain.content.base.entity.MediaType;

@Getter
@ToString
@Builder
public class ContentDetailResponse {
    private MediaType mediaType;
    private ContentDetail contentDetail;
    private MemberInteraction memberInteraction;
//    private DataSource dataSource;
}
