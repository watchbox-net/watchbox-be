package net.watchpeople.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchpeople.domain.search.dto.request.SearchType;
import net.watchpeople.domain.search.dto.response.list.MultiSearchResponse;
import net.watchpeople.domain.search.dto.response.list.SearchListResponse;
import net.watchpeople.domain.tmdb.dto.TmdbContentResultDto;
import net.watchpeople.domain.tmdb.dto.TmdbSearchResponseDto;
import net.watchpeople.domain.tmdb.service.TmdbSearchService;
import net.watchpeople.domain.tmdb.util.TmdbConverterUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final TmdbSearchService tmdbSearchService;

    /**
     * 검색 유형에 따른 콘텐츠 검색
     *
     * TmdbSearchResponseDto
     * ├─ page: 현재 페이지 번호
     * ├─ totalResults: 전체 검색 결과 수
     * ├─ totalPages: 전체 페이지 수
     * └─ results: List<TmdbContentResultDto>
     *
     * TmdbContentResultDto
     * ├─ 영화/TV/인물 중 하나의 단일 검색 결과
     * └─ 공통 필드 + 타입별 고유 필드
     *
     * MultiSearchResponse (응답 DTO - 부모 클래스)
     * ├─ MovieSearchResponse: 영화 검색 결과
     * ├─ TvSearchResponse: TV 검색 결과
     * └─ PersonSearchResponse: 인물 검색 결과
     */
    public SearchListResponse searchContentList(SearchType searchType, String query, Integer page) {
        TmdbSearchResponseDto tmdbResponseDto = getSearchResponse(searchType, query, page);
        List<MultiSearchResponse> contentList = convertToResponseList(searchType, tmdbResponseDto.getResults());

        return SearchListResponse.builder()
                .page(tmdbResponseDto.getPage())
                .totalResults(tmdbResponseDto.getTotalResults())
                .totalPages(tmdbResponseDto.getTotalPages())
                .contentList(contentList)
                .build();
    }

    private TmdbSearchResponseDto getSearchResponse(SearchType searchType, String query, Integer page) {
        return switch (searchType) {
            case MULTI -> tmdbSearchService.searchMulti(query, page);
            case MOVIE -> tmdbSearchService.searchMovie(query, page);
            case TV -> tmdbSearchService.searchTv(query, page);
            case PERSON -> tmdbSearchService.searchPerson(query, page);
        };
    }

    private List<MultiSearchResponse> convertToResponseList(SearchType searchType, List<TmdbContentResultDto> results) {
        return switch (searchType) {
            case MULTI -> results.stream()
                    .map(this::convertByMediaType)
                    .collect(Collectors.toList());
            case MOVIE -> results.stream()
                    .map(TmdbConverterUtil::convertToMovieSearchResponse)
                    .collect(Collectors.toList());
            case TV -> results.stream()
                    .map(TmdbConverterUtil::convertToTvSearchResponse)
                    .collect(Collectors.toList());
            case PERSON -> results.stream()
                    .map(TmdbConverterUtil::convertToPersonSearchResponse)
                    .collect(Collectors.toList());
        };
    }

    private MultiSearchResponse convertByMediaType(TmdbContentResultDto result) {
        return switch (result.getMediaType()) {
            case "movie" -> TmdbConverterUtil.convertToMovieSearchResponse(result);
            case "tv" -> TmdbConverterUtil.convertToTvSearchResponse(result);
            case "person" -> TmdbConverterUtil.convertToPersonSearchResponse(result);
            default -> {
                log.info("ID: {}, Unknown media type: {}", result.getId(), result.getMediaType());
                yield null;
            }
        };
    }

}
