package net.watchbox.domain.search.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.content.base.mapper.tmdb.TmdbSearchDtoMapper;
import net.watchbox.domain.search.dto.request.SearchType;
import net.watchbox.global.tmdb.response.search.TmdbSearchCommonResponse;
import net.watchbox.global.tmdb.service.TmdbSearchService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchFacade {
//    private final OldSearchContentService oldSearchContentService;
    private final TmdbSearchService tmdbSearchService;

//    public ContentSearchPageResponse searchContentList(SearchType searchType, String query, Integer page) {
//        return oldSearchContentService.searchContentList(searchType, query, page);
//    }

    public ContentPageResponse searchMultiList(String query, Integer page) {
        TmdbSearchCommonResponse tmdbResponse = tmdbSearchService.searchMulti(query, page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbSearchDtoMapper.toContentItemList(tmdbResponse, SearchType.MULTI))
                .totalCount(tmdbResponse.getTotalResults())
                .totalPages(tmdbResponse.getTotalPages())
                .currentPage(tmdbResponse.getPage())
                .build();
    }

    public ContentPageResponse searchMovieList(String query, Integer page) {
        TmdbSearchCommonResponse tmdbResponse = tmdbSearchService.searchMovie(query, page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbSearchDtoMapper.toContentItemList(tmdbResponse, SearchType.MOVIE))
                .totalCount(tmdbResponse.getTotalResults())
                .totalPages(tmdbResponse.getTotalPages())
                .currentPage(tmdbResponse.getPage())
                .build();
    }

    public ContentPageResponse searchTvList(String query, Integer page) {
        TmdbSearchCommonResponse tmdbResponse = tmdbSearchService.searchTv(query, page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbSearchDtoMapper.toContentItemList(tmdbResponse, SearchType.TV))
                .totalCount(tmdbResponse.getTotalResults())
                .totalPages(tmdbResponse.getTotalPages())
                .currentPage(tmdbResponse.getPage())
                .build();
    }

    public ContentPageResponse searchPersonList(String query, Integer page) {
        TmdbSearchCommonResponse tmdbResponse = tmdbSearchService.searchPerson(query, page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbSearchDtoMapper.toContentItemList(tmdbResponse, SearchType.PERSON))
                .totalCount(tmdbResponse.getTotalResults())
                .totalPages(tmdbResponse.getTotalPages())
                .currentPage(tmdbResponse.getPage())
                .build();
    }

}
