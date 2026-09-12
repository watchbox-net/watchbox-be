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
import net.watchbox.global.tmdb.response.people.TmdbPersonDetailsResponse;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbSearchService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    /**
     * 상세 조회는 TMDB 두 건을 <b>병렬</b>로 부른다.
     *
     * <p>두 응답은 서로를 참조하지 않아 순서에 의미가 없다. 순차로 부르면 응답시간이 두 건의 합이 되는데,
     * 실측(prod)에서 details 1.12s + images 0.39s 로 상세 페이지가 1.6s 였다. zip 은 느린 쪽에 수렴한다.
     * (사람 상세는 API 1 응답을 API 2 요청에 써야 해서 직렬이다 — {@link #fromPersonDetails})
     *
     * <p>홈({@code HomeTmdbSectionLoader})과 달리 실패를 격리하지 않는다. 둘 중 하나만 없어도
     * 상세 페이지를 못 그리므로 zip 의 fail-fast 가 맞다.
     */
    public ContentInfo fromMovieDetails(Long tmdbId) {
        // (API 1) Details & ko-KR & credits,watch/providers,videos -> 출연진/제작진, 플랫폼, (비디오)
        // (API 2) Images & null
        return Mono.zip(
                        tmdbMoviesService.getMovieDetailsWithCVPMono(tmdbId),
                        tmdbMoviesService.getMovieImagesMono(tmdbId),
                        TmdbContentDetailDtoMapper::toMovieInfo)
                .block();
    }

    /** 병렬 호출 이유는 {@link #fromMovieDetails} 주석 참고. */
    public ContentInfo fromTvDetails(Long tmdbId) {
        // (API 1) Details & ko-KR & aggregate_credits,watch/providers,videos -> 역대 출연진/제작진, 플랫폼, (비디오)
        // (API 2) Images & null
        return Mono.zip(
                        tmdbTvSeriesService.getTvSeriesDetailsWithCVPMono(tmdbId),
                        tmdbTvSeriesService.getTvSeriesImagesMono(tmdbId),
                        TmdbContentDetailDtoMapper::toTvInfo)
                .block();
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
