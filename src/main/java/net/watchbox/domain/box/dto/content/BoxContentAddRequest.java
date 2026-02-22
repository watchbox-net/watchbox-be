package net.watchbox.domain.box.dto.content;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.common.entity.MediaType;

@Getter
@ToString
public class BoxContentAddRequest {
    private Long contentId;
    private MediaType mediaType;
}
