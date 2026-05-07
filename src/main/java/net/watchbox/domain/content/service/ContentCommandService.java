package net.watchbox.domain.content.service;

import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.repository.ContentRepository;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.domain.content.sub.movie.repository.MovieRepository;
import net.watchbox.domain.content.sub.person.entity.Department;
import net.watchbox.domain.content.sub.person.entity.Person;
import net.watchbox.domain.content.sub.person.repository.PersonRepository;
import net.watchbox.domain.content.sub.tv.entity.Tv;
import net.watchbox.domain.content.sub.tv.repository.TvRepository;
import net.watchbox.global.tmdb.inner.search.TmdbSearchResultItem;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.global.tmdb.response.people.TmdbPersonDetailsResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbSearchService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import net.watchbox.global.tmdb.util.Country;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@Observed
public class ContentCommandService {
    private final ContentRepository contentRepository;

    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final PersonRepository personRepository;

    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;
    private final TmdbSearchService tmdbSearchService;

    /**
     * Content DB 존재 여부 확인하여 조회 or 저장
     * - 존재하면 패스
     * - 존재하지 않으면
     *    1) Content 정보 저장
     *    2) SubContents 저장 or ToDo) 요청 이벤트 발행 요청 이벤트 발행
     */
    public Content getOrSaveContentCascade(Long tmdbId, MediaType mediaType) {
        return contentRepository.findByTmdbIdAndMediaType(tmdbId, mediaType).orElseGet(() -> {
            // 1) Content 정보 저장
            Content content = saveContent(tmdbId, mediaType);

            // 2) Content의 하위 엔티티 저장
            saveSubContents(content, mediaType);

            content.markAsSaved();
            return content;
        });
    }

    // ToDo: (위변조 방지) 일단 만들어놓고 나중에 tmdb api 검색에서 없으면 위변조로 간주하고 DB에서 삭제할 것
    @Observed
    public Content saveContent(Long tmdbId, MediaType mediaType) {
        return contentRepository.save(
                Content.builder()
                        .tmdbId(tmdbId)
                        .mediaType(mediaType)
                        .isSaved(false)
                        .build()
        );
    }

    public void saveSubContents(Content content, MediaType mediaType) {
        switch (mediaType) {
            case MOVIE -> {
                TmdbMoviesDetailsResponse response = tmdbMoviesService.getMovieDetails(content.getTmdbId());
                saveMovieContent(content, response);
            }
            case TV -> {
                TmdbTvSeriesDetailsResponse response = tmdbTvSeriesService.getTvSeriesDetails(content.getTmdbId());
                saveTvContent(content, response);
            }
            case PERSON -> {
                // API 1 응답을 API 2 요청에서 이용해야하므로 직렬로 요청해야한다.
                // (API 1) Detail & ko-KR -> 기본 정보
                TmdbPersonDetailsResponse detailsResponse = tmdbPeopleService.getPeopleDetails(content.getTmdbId());

                // (API 2) Search/Person & en-US & query=nameKo -> ID 일치에서 nameEn, nameOriginal 가져오기
                Optional<TmdbSearchResultItem> matched = tmdbSearchService.searchPersonExtraNames(detailsResponse.getName(), content.getTmdbId());
                String nameEn = matched.map(TmdbSearchResultItem::getName).orElse(null);
                String nameOriginal = matched.map(TmdbSearchResultItem::getOriginalName).orElse(null);

                savePersonContent(content, detailsResponse, nameEn, nameOriginal);
            }
        }
    }

    public void saveMovieContent(Content content, TmdbMoviesDetailsResponse tmdbMovieDetail) {
        movieRepository.save(Movie.builder()
                .tmdbId(content.getTmdbId())
                .content(content)
                .titleKo(tmdbMovieDetail.getTitle())
//                .titleEn(tmdbMovieDetail.getOriginalTitle()) // ToDo: en-US 데이터 추가 요청 필요
                .titleOriginal(tmdbMovieDetail.getOriginalTitle())
                .posterPath(tmdbMovieDetail.getPosterPath())
                .popularity(tmdbMovieDetail.getPopularity())
                .voteAverage(tmdbMovieDetail.getVoteAverage())
                .voteCount(tmdbMovieDetail.getVoteCount())
                .releaseDate(LocalDate.parse(tmdbMovieDetail.getReleaseDate()))
                .originCountry(tmdbMovieDetail.getOriginCountry().isEmpty() ? null :
                        Country.fromCode(tmdbMovieDetail.getOriginCountry().get(0)))
                .genreIds(tmdbMovieDetail.getGenres().stream().map(g -> g.getId().intValue()).toList())
                .build());
    }

    public void saveTvContent(Content content, TmdbTvSeriesDetailsResponse tmdbTvSeriesDetail) {
        tvRepository.save(Tv.builder()
                .tmdbId(content.getTmdbId())
                .content(content)
                .nameKo(tmdbTvSeriesDetail.getName())
//                .nameEn(tmdbTvSeriesDetail.getOriginalName()) // ToDo: en-US 데이터 추가 요청 필요
                .nameOriginal(tmdbTvSeriesDetail.getOriginalName())
                .posterPath(tmdbTvSeriesDetail.getPosterPath())
                .popularity(tmdbTvSeriesDetail.getPopularity())
                .voteAverage(tmdbTvSeriesDetail.getVoteAverage())
                .voteCount(tmdbTvSeriesDetail.getVoteCount())
                .firstAirDate(LocalDate.parse(tmdbTvSeriesDetail.getFirstAirDate()))
                .lastAirDate(LocalDate.parse(tmdbTvSeriesDetail.getLastAirDate()))
                .numberOfSeasons(tmdbTvSeriesDetail.getNumberOfSeasons())
                .originCountry(tmdbTvSeriesDetail.getOriginCountry().isEmpty() ? null :
                        Country.fromCode(tmdbTvSeriesDetail.getOriginCountry().get(0)))
                .genreIds(tmdbTvSeriesDetail.getGenres().stream().map(g -> g.getId().intValue()).toList())
                .build());
    }

    public void savePersonContent(Content content, TmdbPersonDetailsResponse tmdbPersonDetail, String nameEn, String nameOriginal) {
        personRepository.save(Person.builder()
                .tmdbId(content.getTmdbId())
                .content(content)
                .nameKo(tmdbPersonDetail.getName())
                .nameEn(nameEn)
                .nameOriginal(nameOriginal)
                .profilePath(tmdbPersonDetail.getProfilePath())
                .knownForDepartment(Department.fromEnglishValue(tmdbPersonDetail.getKnownForDepartment()))
                .popularity(tmdbPersonDetail.getPopularity())
                .birthday(tmdbPersonDetail.getBirthday() == null ? null :
                        LocalDate.parse(tmdbPersonDetail.getBirthday()))
                .placeOfBirth(tmdbPersonDetail.getPlaceOfBirth())
                .build());
    }

    public void deleteContentCascade(Long tmdbId) {
        contentRepository.deleteById(tmdbId);
        log.info("Deleted Content and all associated sub-entities for TMDB ID: {}", tmdbId);
    }

}
