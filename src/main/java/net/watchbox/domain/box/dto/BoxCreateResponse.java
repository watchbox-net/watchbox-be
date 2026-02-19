package net.watchbox.domain.box.dto;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.BoxType;
import net.watchbox.domain.box.entity.VisibleType;

@Getter
@ToString
public class BoxCreateResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;
    private VisibleType visibleType;
    private Long ownerId;

    public static BoxCreateResponse from(Box box, Long ownerId) {
        BoxCreateResponse response = new BoxCreateResponse();
        response.boxId = box.getBoxId();
        response.name = box.getName();
        response.description = box.getDescription();
        response.boxType = box.getBoxType();
        response.visibleType = box.getVisibleType();
        response.ownerId = ownerId;
        return response;
    }
}
