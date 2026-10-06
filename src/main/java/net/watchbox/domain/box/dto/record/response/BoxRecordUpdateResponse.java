package net.watchbox.domain.box.dto.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 실제로 반영된 것만 담는다. 이미 담겨 있던 콘텐츠를 또 추가 요청하거나, 내가 담지 않은 것을
 * 삭제 요청한 경우는 조용히 건너뛰므로 <b>요청 목록과 응답 목록이 다를 수 있다.</b>
 * 프론트는 응답 기준으로 체크 상태를 맞추면 된다.
 */
@Getter
@ToString
@Builder
public class BoxRecordUpdateResponse {
    private Long boxId;
    private Long memberId;
    private List<Long> addedContentIds;
    private List<Long> removedContentIds;
}
