package net.watchbox.domain.box.dto.response.my;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MyBoxContentPageResponse {
    private Long totalCount;
    private List<MyBoxContentItem> items;
}
