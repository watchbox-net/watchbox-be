package net.watchbox.domain.record.dto.response;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.base.entity.MediaType;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.entity.WatchStatus;

import java.time.LocalDate;

@Getter
@ToString
public class ContentRecordResponse {
    private Long watchRecordId;
    private WatchStatus watchStatus;
    private LocalDate watchedDate;
    private Boolean liked;
    private Long memberId;
    private Long contentId;
    private MediaType mediaType;

    public static ContentRecordResponse from(ContentRecord contentRecord){
        ContentRecordResponse response = new ContentRecordResponse();
        response.watchRecordId = contentRecord.getContentRecordId();
        response.watchStatus = contentRecord.getWatchStatus();
        response.watchedDate = contentRecord.getWatchedDate();
        response.liked = contentRecord.getLiked();
        response.memberId = contentRecord.getMember().getMemberId();
        response.contentId = contentRecord.getContent().getTmdbId();
        response.mediaType = contentRecord.getMediaType();
        return response;
    }
}
