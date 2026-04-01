package net.watchbox.domain.box.dto.box;


import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
public class MyBoxResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;
    private List<String> previewPosterList; // 3개
    private LocalDateTime lastContentAddedAt;

    public static MyBoxResponse from(Box box) {
        MyBoxResponse response = new MyBoxResponse();
        response.boxId = box.getBoxId();
        response.name = box.getName();
        response.description = box.getDescription();
        response.boxType = box.getBoxType();
        return response;
    }
}
