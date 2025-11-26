package net.watchpeople.domain.tmdb.util;

import net.watchpeople.domain.content.MediaType;
import net.watchpeople.domain.search.dto.response.list.*;
import net.watchpeople.domain.tmdb.dto.TmdbContentResultDto;

import static net.watchpeople.domain.tmdb.util.TmdbUtils.buildImageFullUrl;
import static net.watchpeople.global.util.ConvertUtils.parseReleaseDate;

public final class TmdbConverterUtil {
//    /**
//     * TMDB 응답을 SearchListResponse로 변환
//     */
//    public static SearchListResponse convertToSearchListResponse(TmdbSearchResponseDto tmdbResponse) {
//        List<MultiSearchResponse> contentList = tmdbResponse.getResults().stream()
//                .map(TmdbConverterUtil::convertToMovieSearchResponse)
//                .collect(Collectors.toList());
//
//        return SearchListResponse.builder()
//                .page(tmdbResponse.getPage())
//                .totalResults(tmdbResponse.getTotalResults())
//                .totalPages(tmdbResponse.getTotalPages())
//                .contentList(contentList)
//                .build();
//    }

    /**
     * TmdbResultDto → MovieSearchResponse 변환
     */
    public static MovieSearchResponse convertToMovieSearchResponse(TmdbContentResultDto result) {
        return MovieSearchResponse.builder()
                .id(result.getId())             // 부모 필드
                .mediaType(MediaType.MOVIE)     // 부모 필드
                .title(result.getTitle())
                .originalTitle(result.getOriginalTitle())
                .overview(result.getOverview())
                .genreIds(result.getGenreIds())
                .posterPath(buildImageFullUrl(result.getPosterPath()))
                .releaseDate(parseReleaseDate(result.getReleaseDate()))
                .build();
    }

    /**
     * TmdbResultDto → TvSearchResponse 변환
     */
    public static TvSearchResponse convertToTvSearchResponse(TmdbContentResultDto result) {
        return TvSearchResponse.builder()
                .id(result.getId())             // 부모 필드
                .mediaType(MediaType.TV)        // 부모 필드
                .name(result.getName())
                .originalName(result.getOriginalName())
                .overview(result.getOverview())
                .genreIds(result.getGenreIds())
                .posterPath(buildImageFullUrl(result.getPosterPath()))
                .firstAirDate(parseReleaseDate(result.getFirstAirDate()))
                .build();
    }

    /**
     * TmdbResultDto → PersonSearchResponse 변환
     */
    public static PersonSearchResponse convertToPersonSearchResponse(TmdbContentResultDto result) {
        return PersonSearchResponse.builder()
                .id(result.getId())             // 부모 필드
                .mediaType(MediaType.PERSON)    // 부모 필드
                .name(result.getName())
                .profilePath(buildImageFullUrl(result.getProfilePath()))
                .build();
    }
}
