package net.watchbox.domain.box.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class MyBoxListResponse {
    List<MyBoxResponse> boxList;
    int boxCount;
}
