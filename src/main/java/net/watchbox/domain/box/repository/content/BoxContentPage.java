package net.watchbox.domain.box.repository.content;

import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.global.dto.CursorPayload;

import java.util.List;

/**
 * BoxContent 페이지 결과.
 * - boxContents: 페이지에 속한 모든 BoxContent (contentId 단위로 그룹핑되어 있음, 같은 그룹은 publisher 표시 순서를 위해 createdAt ASC)
 * - hasNext: 다음 페이지 존재 여부
 * - nextCursor: 다음 페이지 커서 (없으면 null)
 */
public record BoxContentPage(
        List<BoxContent> boxContents,
        boolean hasNext,
        CursorPayload nextCursor
) {
    public static BoxContentPage empty() {
        return new BoxContentPage(List.of(), false, null);
    }
}
