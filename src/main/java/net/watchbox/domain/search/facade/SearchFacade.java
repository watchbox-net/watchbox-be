package net.watchbox.domain.search.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.search.dto.request.SearchType;
import net.watchbox.domain.search.dto.response.list.SearchListResponse;
import net.watchbox.domain.search.service.SearchContentService;
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
