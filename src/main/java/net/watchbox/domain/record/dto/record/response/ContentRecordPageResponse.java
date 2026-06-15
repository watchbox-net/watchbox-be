package net.watchbox.domain.record.dto.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentRecordPageResponse {
    private List<ContentRecordResponse> watchRecordList;
    private int totalCount;
    private int totalPages;
    private int currentPage;
}
