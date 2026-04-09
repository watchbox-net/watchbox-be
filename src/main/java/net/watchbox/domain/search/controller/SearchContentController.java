package net.watchbox.domain.search.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
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
@Tag(name = "SearchContent", description = "컨텐츠 검색 API")
public class SearchContentController {
    private final SearchFacade searchFacade;

    /**
     * 리스트 검색 ~ SEARCH API 요청
     * 1. Multi 검색
     * 2. Movie 검색
     * 3. TV 검색
     * 4. Person 검색
     */

    @GetMapping("/multi")
    public ResponseEntity<ApiResponse<ContentPageResponse>> searchMultiList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchMultiList(query, page))
        );
    }

    @GetMapping("/movie")
    public ResponseEntity<ApiResponse<ContentPageResponse>> searchMovieList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchMovieList(query, page))
        );
    }

    @GetMapping("/tv")
    public ResponseEntity<ApiResponse<ContentPageResponse>> searchTvList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchTvList(query, page))
        );
    }

    @GetMapping("/person")
    public ResponseEntity<ApiResponse<ContentPageResponse>> searchPersonList(
            @RequestParam String query,
            @RequestParam Integer page
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(searchFacade.searchPersonList(query, page))
        );
    }

    /**
     * 상세 검색 ~ MOVIES / TV SERIES / PEOPLE Details API 요청
     * @RequestParam meadiaType : MOVIE, TV, PERSON
     * @RequestParam tmdbId
     */
//    @GetMapping("/detail")


}
