package net.watchbox.domain.content.dto.detail.credit.work;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class WorkCredit {
    private List<CombinedCredit> combinedCreditList;    // 출연/제작 전체, 최신 날짜순 정렬
    private Long totalCount;
}
