package net.watchbox.domain.box.dto.response.my;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.watchbox.domain.content.common.dto.list.ContentSummary;

@Data
@AllArgsConstructor
public class MyBoxContentItem {
    ContentSummary contentSummary;
    Long myBoxContentId;
}
