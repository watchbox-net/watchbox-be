package net.watchbox.domain.content.dto.box;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.dto.member.BoxMemberResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
public class ContentBoxItem {
    private Long boxId;
    private String name;
    private BoxType boxType;
    private LocalDateTime lastContentAddedAt;
    private List<String> previewPosterList; // 3개
    private List<String> memberNameList; // 공유박스에만 존재
    private boolean hasContent;

    // Content가 저장되어있지 않을 경우 바로 사용
    public static ContentBoxItem of(Box box, List<String> posterList, boolean hasContent) {
        ContentBoxItem item = new ContentBoxItem();
        item.boxId = box.getBoxId();
        item.name = box.getName();
        item.boxType = box.getBoxType();
        item.lastContentAddedAt = LocalDateTime.now();
        item.previewPosterList = posterList;
        if (box.getBoxType() == BoxType.SHARED) { // 공유 박스일 경우
            item.memberNameList = box.getBoxMembers().stream()
                    .map(boxMember -> boxMember.getMember().getNickname())
                    .toList();
        }
        item.hasContent = hasContent;
        return item;
    }
}
