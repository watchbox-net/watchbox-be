package net.watchbox.domain.box.dto;


import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.BoxType;

@Getter
@ToString
public class MyBoxResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;

    public static MyBoxResponse from(Box box) {
        MyBoxResponse response = new MyBoxResponse();
        response.boxId = box.getBoxId();
        response.name = box.getName();
        response.description = box.getDescription();
        response.boxType = box.getBoxType();
        return response;
    }
}
