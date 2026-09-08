package net.watchbox.domain.content.dto.list;

import lombok.*;
import net.watchbox.domain.content.dto.interaction.PublisherSummary;
import net.watchbox.domain.content.dto.interaction.MemberRecord;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentItem {
    private ContentSummary contentSummary;
    private MemberRecord memberRecord;

//    private BoxMeta boxMeta;
    private List<PublisherSummary> publisherSummaryList; // 공유 박스 콘텐츠 조회시에만 사용
    private boolean hasAddedInbox; // 박스 포함 유무 (로그인 사용자 기준) | 사용처 - 상세 페이지, 홈화면, 박스에 추가할 콘텐츠 검색 페이지
//    private Long boxContentId;
}
