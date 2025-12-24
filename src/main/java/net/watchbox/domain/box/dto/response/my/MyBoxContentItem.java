package net.watchbox.domain.box.dto.response.my;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.watchbox.domain.box.dto.response.ContentItem;

@Data
@AllArgsConstructor
public class MyBoxContentItem {
    ContentItem contentItem;
    Long myBoxContentId;
}
