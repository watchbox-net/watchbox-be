package net.watchbox.domain.member.dto.response.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class MemberSearchPageResponse {
    private List<MemberSearchResponse> memberSearchList;
    private int totalCount;
    private int totalPages;
    private int currentPage;
}
