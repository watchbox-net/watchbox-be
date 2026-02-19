package net.watchbox.domain.box.dto;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.BoxType;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class SharedBoxResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;

    private List<BoxMemberResponse> members;

    public static SharedBoxResponse from(Box box){
        SharedBoxResponse sharedBoxResponse = new SharedBoxResponse();
        sharedBoxResponse.boxId = box.getBoxId();
        sharedBoxResponse.name = box.getName();
        sharedBoxResponse.description = box.getDescription();
        sharedBoxResponse.boxType = box.getBoxType();
        sharedBoxResponse.members = box.getBoxMembers().stream()
                .map(BoxMemberResponse::from)
                .collect(Collectors.toList());
        return sharedBoxResponse;
    }
}
