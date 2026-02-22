package net.watchbox.domain.box.dto.response.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.watchbox.domain.content.common.dto.interaction.PublisherSummary;
import net.watchbox.domain.content.common.dto.list.ContentSummary;

import java.util.List;

@Data
@AllArgsConstructor
public class SharedBoxContentItem {
    ContentSummary contentSummary;
    Long sbcId;
    List<PublisherSummary> adders;
}
