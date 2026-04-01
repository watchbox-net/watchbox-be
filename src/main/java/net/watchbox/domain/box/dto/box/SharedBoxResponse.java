package net.watchbox.domain.box.dto.box;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.dto.member.BoxMemberResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class SharedBoxResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;
    private List<BoxMemberResponse> memberList;
    private List<String> previewPosterList; // 3개

    public static SharedBoxResponse from(Box box){
        SharedBoxResponse sharedBoxResponse = new SharedBoxResponse();
        sharedBoxResponse.boxId = box.getBoxId();
        sharedBoxResponse.name = box.getName();
        sharedBoxResponse.description = box.getDescription();
        sharedBoxResponse.boxType = box.getBoxType();
        sharedBoxResponse.memberList = box.getBoxMembers().stream()
                .map(BoxMemberResponse::from)
                .collect(Collectors.toList());
        return sharedBoxResponse;
    }
}
