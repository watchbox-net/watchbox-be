package net.watchbox.domain.search.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.search.dto.request.SearchType;
import net.watchbox.domain.search.dto.response.list.SearchListResponse;
import net.watchbox.domain.search.facade.SearchFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search/contents")
public class SearchContentController {
    private final SearchFacade searchFacade;

    /**
     * 1. 통합 검색
     * 2. 영화 검색
     * 3. TV 검색
     * 4. 인물 검색
     */
    @GetMapping("/multi")
    public ResponseEntity<ApiResponse<SearchListResponse>> searchMultiList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchContentList(SearchType.MULTI, query, page))
        );
    }

    @GetMapping("/movie")
    public ResponseEntity<ApiResponse<SearchListResponse>> searchMovieList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchContentList(SearchType.MOVIE, query, page))
        );
    }

    @GetMapping("/tv")
    public ResponseEntity<ApiResponse<SearchListResponse>> searchTvList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchContentList(SearchType.TV, query, page))
        );
    }

    @GetMapping("/person")
    public ResponseEntity<ApiResponse<SearchListResponse>> searchPersonList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchContentList(SearchType.PERSON, query, page))
        );
    }

    // 통합 검색
//    @GetMapping("/multi")
//    public ResponseEntity<ApiResponse<List<SearchResponse>>> searchMulti(
//            @RequestParam String query
//    ) {
//        return ResponseEntity.ok(
//                ApiResponse.success(searchService.searchMulti(
//                        MultiSearchRequest.builder()
//                                .query(query)
//                                .type("multi")
//                                .build()
//                ))
//        );
//    }
}
