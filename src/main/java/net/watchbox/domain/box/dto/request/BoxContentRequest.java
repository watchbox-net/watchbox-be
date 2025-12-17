package net.watchbox.domain.box.dto.request;

import lombok.Getter;
import net.watchbox.domain.content.common.entity.MediaType;

@Getter
public class BoxContentRequest {
    private Long contentId;
    private MediaType mediaType;
}
