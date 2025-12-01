package net.watchpeople.domain.search.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchpeople.domain.search.dto.request.SearchType;
import net.watchpeople.domain.search.dto.response.list.SearchListResponse;
import net.watchpeople.domain.search.service.SearchContentService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchFacade {
    private final SearchContentService searchContentService;

    public SearchListResponse searchContentList(SearchType searchType, String query, Integer page) {
        return searchContentService.searchContentList(searchType, query, page);
    }

}
