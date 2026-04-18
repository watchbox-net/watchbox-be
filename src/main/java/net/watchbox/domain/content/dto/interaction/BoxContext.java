package net.watchbox.domain.content.dto.interaction;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class BoxContext {
    private List<PublisherSummary> publisherSummaryList; // 공유 박스 컨텐츠 조회시에만 사용
    private boolean hasAddedInbox; // 박스 포함 유무 (로그인 사용자 기준) | 사용처 - 상세 페이지, 홈화면, 박스에 추가할 컨텐츠 검색 페이지
}
