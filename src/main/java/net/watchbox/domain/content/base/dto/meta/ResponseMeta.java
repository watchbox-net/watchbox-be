package net.watchbox.domain.content.base.dto.meta;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class ResponseMeta {
    private ContentType contentType;
    private DataSource dataSource; // ContentMeta로 옮겨야할듯
    private Set<ContentItemField> contentItemIncluded;
//    private Instant fetchedAt;           // 캐시 신선도 확인용으로도 활용 가능
}
