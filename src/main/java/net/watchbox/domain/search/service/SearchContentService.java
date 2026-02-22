package net.watchbox.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.search.dto.request.SearchType;
import net.watchbox.domain.search.dto.response.list.MultiSearchResponse;
import net.watchbox.domain.search.dto.response.list.ContentSearchPageResponse;
import net.watchbox.domain.tmdb.inner.search.TmdbSearchResultItem;
import net.watchbox.domain.tmdb.response.search.TmdbSearchCommonResponse;
import net.watchbox.domain.tmdb.service.TmdbSearchService;
import net.watchbox.domain.tmdb.util.TmdbResponseConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class SearchContentService {
    private final TmdbSearchService tmdbSearchService;

    /**
     * 검색 유형에 따른 콘텐츠 검색
     *
     * TmdbSearchCommonResponse
     * ├─ page: 현재 페이지 번호
     * ├─ totalResults: 전체 검색 결과 수
     * ├─ totalPages: 전체 페이지 수
     * └─ results: List<TmdbSearchCommonResponse>
     *
     * TmdbSearchResultItem
     * ├─ 영화/TV/인물 중 하나의 단일 검색 결과
     * └─ 공통 필드 + 타입별 고유 필드
     *
     * MultiSearchResponse (부모 클래스 응답 DTO)
     * ├─ MovieSearchResponse: 영화 검색 결과
     * ├─ TvSearchResponse: TV 검색 결과
     * └─ PersonSearchResponse: 인물 검색 결과
     */
    public ContentSearchPageResponse searchContentList(SearchType searchType, String query, Integer page) {
        TmdbSearchCommonResponse tmdbResponseDto = getSearchResponse(searchType, query, page);
        List<MultiSearchResponse> contentList = convertToResponseList(searchType, tmdbResponseDto.getResults());

        return ContentSearchPageResponse.builder()
                .page(tmdbResponseDto.getPage())
                .totalResults(tmdbResponseDto.getTotalResults())
                .totalPages(tmdbResponseDto.getTotalPages())
                .contentList(contentList)
                .build();
    }

    private TmdbSearchCommonResponse getSearchResponse(SearchType searchType, String query, Integer page) {
        return switch (searchType) {
            case MULTI -> tmdbSearchService.searchMulti(query, page);
            case MOVIE -> tmdbSearchService.searchMovie(query, page);
            case TV -> tmdbSearchService.searchTv(query, page);
            case PERSON -> tmdbSearchService.searchPerson(query, page);
        };
    }

    private List<MultiSearchResponse> convertToResponseList(SearchType searchType, List<TmdbSearchResultItem> results) {
        return switch (searchType) {
            case MULTI -> results.stream()
                    .map(this::convertByMediaType)
                    .collect(Collectors.toList());
            case MOVIE -> results.stream()
                    .map(TmdbResponseConverter::convertToMovieSearchResponse)
                    .collect(Collectors.toList());
            case TV -> results.stream()
                    .map(TmdbResponseConverter::convertToTvSearchResponse)
                    .collect(Collectors.toList());
            case PERSON -> results.stream()
                    .map(TmdbResponseConverter::convertToPersonSearchResponse)
                    .collect(Collectors.toList());
        };
    }

    private MultiSearchResponse convertByMediaType(TmdbSearchResultItem result) {
        return switch (result.getMediaType()) {
            case "movie" -> TmdbResponseConverter.convertToMovieSearchResponse(result);
            case "tv" -> TmdbResponseConverter.convertToTvSearchResponse(result);
            case "person" -> TmdbResponseConverter.convertToPersonSearchResponse(result);
            default -> {
                log.info("ID: {}, Unknown media type: {}", result.getId(), result.getMediaType());
                yield null;
            }
        };
    }

}
