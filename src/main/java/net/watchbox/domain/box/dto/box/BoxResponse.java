package net.watchbox.domain.box.dto.box;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.dto.member.BoxMemberResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class BoxResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;
    private LocalDateTime lastContentAddedAt;
    private List<String> previewPosterList; // 3개
    private List<BoxMemberResponse> memberList; // 공유박스에만 존재

    // 박스 생성/수정 응답
    public static BoxResponse from(Box box) {
        BoxResponse boxResponse = new BoxResponse();
        boxResponse.boxId = box.getBoxId();
        boxResponse.name = box.getName();
        boxResponse.description = box.getDescription();
        boxResponse.boxType = box.getBoxType();
        boxResponse.lastContentAddedAt = box.getLastContentAddedAt();
        return boxResponse;
    }

    // 박스 페이지 마이 박스 응답
    public static BoxResponse of(Box box, List<String> previewPosters){
       BoxResponse boxResponse = new BoxResponse();
       boxResponse.boxId = box.getBoxId();
       boxResponse.name = box.getName();
       boxResponse.description = box.getDescription();
       boxResponse.boxType = box.getBoxType();
       boxResponse.lastContentAddedAt = box.getLastContentAddedAt();
       boxResponse.previewPosterList = previewPosters;
       return boxResponse;
    }

    // 박스 페이지 공유 박스 응답
    public static BoxResponse of(Box box, List<String> previewPosters,
                                 List<BoxMemberResponse> memberList){
        BoxResponse boxResponse = new BoxResponse();
        boxResponse.boxId = box.getBoxId();
        boxResponse.name = box.getName();
        boxResponse.description = box.getDescription();
        boxResponse.boxType = box.getBoxType();
        boxResponse.lastContentAddedAt = box.getLastContentAddedAt();
        boxResponse.previewPosterList = previewPosters;
        boxResponse.memberList = memberList;
        return boxResponse;
    }
}
