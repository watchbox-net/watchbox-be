package net.watchbox.domain.box.dto.box;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.box.VisibleType;

@Getter
@ToString
public class BoxUpdateResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;
    private VisibleType visibleType;

    public static BoxUpdateResponse from(Box box) {
        BoxUpdateResponse response = new BoxUpdateResponse();
        response.boxId = box.getBoxId();
        response.name = box.getName();
        response.description = box.getDescription();
        response.boxType = box.getBoxType();
        response.visibleType = box.getVisibleType();
        return response;
    }
}
