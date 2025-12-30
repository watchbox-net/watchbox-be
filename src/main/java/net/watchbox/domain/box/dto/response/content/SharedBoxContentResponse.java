package net.watchbox.domain.box.dto.response.content;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SharedBoxContentResponse {
    private Long totalCount;
    private List<SharedBoxContentItem> items;
}
