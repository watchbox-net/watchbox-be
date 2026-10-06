package net.watchbox.domain.box.dto.record.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * 체크 상태 변경분. 화면에서 체크/해제한 것만 담아 보낸다.
 *
 * <p><b>tmdbId 가 아니라 contentId 를 받는다.</b> 시청 기록에 있다는 것은 Content 가 이미
 * 저장돼 있다는 뜻이라, TMDB 를 다시 조회할 이유가 없다.
 */
@Getter
@ToString
public class BoxRecordDiffRequest {

    @Schema(description = "박스에 추가할 콘텐츠 ID 목록", example = "[1, 2]")
    private List<Long> addContentIds;

    @Schema(description = "박스에서 삭제할 콘텐츠 ID 목록", example = "[3]")
    private List<Long> removeContentIds;

    public List<Long> addContentIdsOrEmpty() {
        return addContentIds == null ? Collections.emptyList() : addContentIds;
    }

    public List<Long> removeContentIdsOrEmpty() {
        return removeContentIds == null ? Collections.emptyList() : removeContentIds;
    }
}
