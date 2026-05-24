package net.watchbox.domain.box.dto.box.response;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.box.VisibleType;

@Getter
@ToString
public class BoxCreateResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;
    private VisibleType visibleType;
    private Long ownerId;

    public static BoxCreateResponse from(Box box) {
        BoxCreateResponse response = new BoxCreateResponse();
        response.boxId = box.getBoxId();
        response.name = box.getName();
        response.description = box.getDescription();
        response.boxType = box.getBoxType();
        response.visibleType = box.getVisibleType();
        response.ownerId = box.getOwner().getMemberId();
        return response;
    }

    @Deprecated
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
