package net.watchbox.domain.box.dto.response.content;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SharedBoxContentItem {
    ContentItem contentItem;
    Long sbcId;
    List<AdderItem> adders;
}
