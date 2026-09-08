package net.watchbox.domain.content.dto.box;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ContentBoxDiffRequest {
    @Schema(description = "콘텐츠를 추가할 박스 ID 목록", example = "[1, 2]")
    private List<Long> addBoxIds;

    @Schema(description = "콘텐츠를 삭제할 박스 ID 목록", example = "[3]")
    private List<Long> removeBoxIds;
}
