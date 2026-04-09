package net.watchbox.domain.box.dto.box;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.dto.member.BoxMemberResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;

import java.time.LocalDateTime;
import java.util.List;

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
        BoxResponse response = new BoxResponse();
        response.boxId = box.getBoxId();
        response.name = box.getName();
        response.description = box.getDescription();
        response.boxType = box.getBoxType();
        response.lastContentAddedAt = box.getLastContentAddedAt();
        return response;
    }

    // 박스 페이지 마이/공유 박스 응답
    public static BoxResponse of(Box box, List<String> previewPosters){
       BoxResponse response = new BoxResponse();
       response.boxId = box.getBoxId();
       response.name = box.getName();
       response.description = box.getDescription();
       response.boxType = box.getBoxType();
       response.lastContentAddedAt = box.getLastContentAddedAt();
       response.previewPosterList = previewPosters;
        if (box.getBoxType() == BoxType.SHARED) { // 공유 박스일 경우
            response.memberList = box.getBoxMembers().stream()
                    .map(BoxMemberResponse::from)
                    .toList();
        }
       return response;
    }
}
