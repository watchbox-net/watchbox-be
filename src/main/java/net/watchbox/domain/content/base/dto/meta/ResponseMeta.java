package net.watchbox.domain.content.base.dto.meta;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class    ResponseMeta { // ToDo: 리스트에 사용되는 메타클래스라고 바꿔야할듯
    private ContentType contentType;
    private DataSource dataSource; // ContentMeta로 네이밍 변경해야함, 상세응답에도 필요함
    private Set<ContentItemField> contentItemIncluded; // 박스용 필드, 시청기록용 필드, 탐색용 필드 나눠야할듯
    // => ContentItemField 중에 응답에 포함되는 항목만 Set에 추가
//    private Instant fetchedAt;           // 캐시 신선도 확인용으로도 활용 가능
}
