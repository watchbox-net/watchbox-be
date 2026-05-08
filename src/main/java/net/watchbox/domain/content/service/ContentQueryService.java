package net.watchbox.domain.content.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.detail.ContentInfo;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.mapper.tmdb.TmdbContentDetailDtoMapper;
import net.watchbox.domain.content.repository.ContentRepository;
import net.watchbox.domain.content.sub.movie.repository.MovieRepository;
import net.watchbox.domain.content.sub.person.repository.PersonRepository;
import net.watchbox.domain.content.sub.tv.repository.TvRepository;
import net.watchbox.global.tmdb.inner.search.TmdbSearchResultItem;
import net.watchbox.global.tmdb.response.common.TmdbWorkImagesResponse;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.global.tmdb.response.people.TmdbPersonDetailsResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbSearchService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentQueryService {
    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final PersonRepository personRepository;
    private final ContentRepository contentRepository;

    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;
    private final TmdbSearchService tmdbSearchService;

    // ============================== Content Saved 여부 조회 ==============================
    public boolean isContentSaved(Long tmdbId, MediaType mediaType) {
        return contentRepository.existsByTmdbIdAndMediaType(tmdbId, mediaType);
    }

    public Optional<Content> findByTmdbIdAndMediaType(Long tmdbId, MediaType mediaType) {
        return contentRepository.findByTmdbIdAndMediaType(tmdbId, mediaType);
    }

    public ContentInfo fromMovieDetails(Long tmdbId) {
        // (API 1) Details & ko-KR & credits,watch/providers,videos -> 출연진/제작진, 플랫폼, (비디오)
        TmdbMoviesDetailsResponse detailsResponse = tmdbMoviesService.getMovieDetailsWithCVP(tmdbId);
        // (API 2) Images & null
        TmdbWorkImagesResponse imagesResponse = tmdbMoviesService.getMovieImages(tmdbId);

        return TmdbContentDetailDtoMapper.toMovieInfo(detailsResponse, imagesResponse);
    }

    public ContentInfo fromTvDetails(Long tmdbId) {
        // (API 1) Details & ko-KR & aggregate_credits,watch/providers,videos -> 역대 출연진/제작진, 플랫폼, (비디오)
        TmdbTvSeriesDetailsResponse detailsResponse = tmdbTvSeriesService.getTvSeriesDetailsWithCVP(tmdbId);
        // (API 2) Images & null
        TmdbWorkImagesResponse imagesResponse = tmdbTvSeriesService.getTvSeriesImages(tmdbId);
        return TmdbContentDetailDtoMapper.toTvInfo(detailsResponse, imagesResponse);
    }

    public ContentInfo fromPersonDetails(Long tmdbId) {
        // API 1 응답을 API 2 요청에서 이용해야하므로 직렬로 요청해야한다.
        // (API 1) Details & ko-KR & combined_credits,images -> 기본 정보, 작품, (이미지)
        TmdbPersonDetailsResponse detailsResponse = tmdbPeopleService.getPeopleDetailsWithCI(tmdbId);

        // (API 2) Search/Person & en-US & query=nameKo -> ID 일치에서 nameEn, nameOriginal 가져오기
        Optional<TmdbSearchResultItem> matched = tmdbSearchService.searchPersonExtraNames(detailsResponse.getName(), tmdbId);
        String nameEn = matched.map(TmdbSearchResultItem::getName).orElse(null);
        String nameOriginal = matched.map(TmdbSearchResultItem::getOriginalName).orElse(null);

        // ToDo: [고도화] (API 3) 대표작 뽑아내기

        return TmdbContentDetailDtoMapper.toPersonInfo(detailsResponse, nameEn, nameOriginal);
    }
}
