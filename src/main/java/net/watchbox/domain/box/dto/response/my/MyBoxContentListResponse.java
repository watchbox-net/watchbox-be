package net.watchbox.domain.box.dto.response.my;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MyBoxContentListResponse {
    private Long totalCount;
    private List<MyBoxContentItem> items;
}
