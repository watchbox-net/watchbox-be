package net.watchbox.domain.content.dto.list;

import lombok.*;
import net.watchbox.domain.content.dto.interaction.PublisherSummary;
import net.watchbox.domain.content.dto.interaction.MemberRecord;

import java.util.List;

@Getter
@ToString
// toBuilder: 개인화를 여러 단계로 얹는다(시청 기록 → 박스 포함 여부).
// 매번 전체 필드를 다시 세우면 한 단계가 앞 단계의 값을 지우는 사고가 난다.
@Builder(toBuilder = true)
public class ContentItem {
    private ContentSummary contentSummary;
    private MemberRecord memberRecord;

//    private BoxMeta boxMeta;
    private List<PublisherSummary> publisherSummaryList; // 공유 박스 콘텐츠 조회시에만 사용
    private boolean hasAddedInbox; // 박스 포함 유무 (로그인 사용자 기준) | 사용처 - 상세 페이지, 홈화면, 박스에 추가할 콘텐츠 검색 페이지
//    private Long boxContentId;
}
