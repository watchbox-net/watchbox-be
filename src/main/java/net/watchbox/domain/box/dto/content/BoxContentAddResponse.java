package net.watchbox.domain.box.dto.content;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.entity.MediaType;

@Getter
@ToString
public class BoxContentAddResponse {
    private Long contentId;
    private MediaType mediaType;
    private Long boxContentId;
    private Long publisherId;

    public static BoxContentAddResponse from(BoxContent boxContent) {
        BoxContentAddResponse response = new BoxContentAddResponse();
        response.contentId = boxContent.getContent().getTmdbId();
        response.mediaType = boxContent.getMediaType();
        response.boxContentId = boxContent.getBoxContentId();
        response.publisherId = boxContent.getPublisher().getMemberId();
        return response;
    }
}
