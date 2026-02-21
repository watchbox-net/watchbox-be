package net.watchbox.domain.box.dto;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.common.entity.MediaType;

@Getter
@ToString
public class BoxContentAddResponse {
    private Long contentId;
    private MediaType mediaType;
    private Long boxContentId;
    private Long addedById;

    public static BoxContentAddResponse from(BoxContent boxContent) {
        BoxContentAddResponse response = new BoxContentAddResponse();
        response.contentId = boxContent.getContent().getTmdbId();
        response.mediaType = boxContent.getMediaType();
        response.boxContentId = boxContent.getBoxContentId();
        response.addedById = boxContent.getAddedBy().getMemberId();
        return response;
    }
}
