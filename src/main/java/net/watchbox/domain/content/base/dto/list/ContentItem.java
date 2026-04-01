package net.watchbox.domain.content.base.dto.list;

import lombok.*;
import net.watchbox.domain.content.base.dto.interaction.PublisherSummary;
import net.watchbox.domain.content.base.dto.interaction.MemberRecord;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentItem {
    private ContentSummary contentSummary;
    private MemberRecord memberRecord;
    private List<PublisherSummary> publisherSummaryList;
    private Long boxContentId;
    private Long contentRecordId;
}
