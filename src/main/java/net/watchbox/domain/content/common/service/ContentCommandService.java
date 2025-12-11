package net.watchbox.domain.content.common.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.common.repository.ContentRepository;
import net.watchbox.domain.content.movie.entity.Movie;
import net.watchbox.domain.content.movie.entity.MovieDetail;
import net.watchbox.domain.content.movie.repository.MovieDetailRepository;
import net.watchbox.domain.content.movie.repository.MovieRepository;
import net.watchbox.domain.content.person.repository.PersonRepository;
import net.watchbox.domain.content.tv.repository.TvRepository;
import net.watchbox.domain.tmdb.response.movies.TmdbMovieDetailsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentCommandService {

    private final ContentRepository contentRepository;

    private final MovieRepository movieRepository;
    private final MovieDetailRepository movieDetailRepository;

    private final TvRepository tvRepository;
    private final PersonRepository personRepository;


    private final ContentQueryService contentQueryService;

    // ToDo: (위변조 방지) 일단 만들어놓고 나중에 tmdb api 검색에서 없으면 위변조로 간주하고 DB에서 삭제할 것
    public Content createContent(Long tmdbId, MediaType mediaType) {
//        contentRepository.insertContent(tmdbId, String.valueOf(mediaType));
        Content content = contentRepository.save(
                Content.builder()
                        .tmdbId(tmdbId)
                        .mediaType(mediaType)
                        .isSaved(false)
                        .build()
        );
//        contentRepository.flush();
        return content;
    }

    public void createSubContent(Content content) {
        // 타입 나누고 해당 상세 정보 요청 받아와서 저장
        switch (content.getMediaType()) {
            case MOVIE -> {
                break;
            }
            case TV -> {
                break;
            }
            case PERSON -> {
                break;
            }
        }
    }

    // TmdbMovieDetailsResponse -> Movie, MovieDetail 저장
    public void saveMovieContent(Content content, TmdbMovieDetailsResponse tmdbMovieDetail) {

        Movie movie = Movie.builder()
                .content(content)
                .titleKo(tmdbMovieDetail.getTitle())
                .titleEn(tmdbMovieDetail.getOriginalTitle())
                .titleOriginal(tmdbMovieDetail.getOriginalTitle())
                .posterPath(tmdbMovieDetail.getPosterPath())
                .popularity(tmdbMovieDetail.getPopularity())
                .voteAverage(tmdbMovieDetail.getVoteAverage())
                .voteCount(tmdbMovieDetail.getVoteCount())
                .year(Integer.parseInt(tmdbMovieDetail.getReleaseDate().substring(0, 4)))
                .genreIds(tmdbMovieDetail.getGenres().stream().map(g -> g.getId().intValue()).toList())
                .build();

        MovieDetail movieDetail = MovieDetail.builder()
                .movie(movie)
                .overview(tmdbMovieDetail.getOverview())
                .backdropPath(tmdbMovieDetail.getBackdropPath())
                .originalLanguage(tmdbMovieDetail.getOriginalLanguage())
                .releaseDate(java.time.LocalDate.parse(tmdbMovieDetail.getReleaseDate()))
                .adult(tmdbMovieDetail.isAdult())
                .video(tmdbMovieDetail.isVideo())  // 추후 매핑 로직 추가 필요
                .status(tmdbMovieDetail.getStatus())
                .runtime(tmdbMovieDetail.getRuntime().intValue())
                .tagline(tmdbMovieDetail.getTagline())
                .homepage(tmdbMovieDetail.getHomepage())
                .imdbId(tmdbMovieDetail.getImdbId())
                .budget(tmdbMovieDetail.getBudget())
                .revenue(tmdbMovieDetail.getRevenue())
                //
                .originCountries(
                        tmdbMovieDetail.getProductionCountries().stream()
                                .map(c -> "\"" + c.getIso31661() + "\"")
                                .toList().toString()
                )
                .productionCompanies(
                        tmdbMovieDetail.getGenres().stream()
                                .map(g -> "{\"id\":" + g.getId() + ",\"name\":\"" + g.getName() + "\"}")
                                .toList().toString()
                )
                .productionCountries(
                        tmdbMovieDetail.getProductionCountries().stream()
                                .map(c -> "{\"iso_3166_1\":\"" + c.getIso31661() + "\",\"name\":\"" + c.getName() + "\"}")
                                .toList().toString()
                )
                .spokenLanguages(
                        tmdbMovieDetail.getGenres().stream()
                                .map(g -> "{\"iso_639_1\":\"" + g.getId() + "\",\"name\":\"" + g.getName() + "\"}")
                                .toList().toString()
                )
                .build();

        movieRepository.save(movie);
        movieDetailRepository.save(movieDetail);
        content.markAsSaved();
    }
}
