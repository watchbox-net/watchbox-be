package net.watchbox.domain.content.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.repository.ContentRepository;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.domain.content.sub.movie.repository.MovieRepository;
import net.watchbox.domain.content.sub.person.entity.Person;
import net.watchbox.domain.content.sub.person.repository.PersonRepository;
import net.watchbox.domain.content.sub.tv.entity.Tv;
import net.watchbox.domain.content.sub.tv.repository.TvRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentQueryService {
    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final PersonRepository personRepository;
    private final ContentRepository contentRepository;

    public Optional<Content> findContentById(Long tmdbId) {
        return contentRepository.findById(tmdbId);
    }

    // ============================== Content <- Id ==============================
    public Movie getMovieByIdOrThrow(Long tmdbId) {
        return movieRepository.findById(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
    }

    public Tv getTvByIdOrThrow(Long tmdbId) {
        return tvRepository.findById(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.TV_NOT_FOUND));
    }

    public Person getPersonByIdOrThrow(Long tmdbId) {
        return personRepository.findById(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.PERSON_NOT_FOUND));
    }

    // ============================== Content, ContentDetail <- Id ==============================
    public Movie getMovieAndMovieDetailByIdOrThrow(Long tmdbId) {
        return movieRepository.findWithDetailByTmdbId(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
    }

    public Tv getTvAndTvDetailByIdOrThrow(Long tmdbId) {
        return tvRepository.findWithDetailByTmdbId(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.TV_NOT_FOUND));
    }

    public Person getPersonAndPersonDetailByIdOrThrow(Long tmdbId) {
        return personRepository.findWithDetailByTmdbId(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.PERSON_NOT_FOUND));
    }

    // ============================== Content Saved 여부 조회 ==============================
    public boolean isContentSaved(Long tmdbId, MediaType mediaType) {
        return contentRepository.existsByTmdbIdAndMediaType(tmdbId, mediaType);
    }

    public Optional<Content> findByTmdbIdAndMediaType(Long tmdbId, MediaType mediaType) {
        return contentRepository.findByTmdbIdAndMediaType(tmdbId, mediaType);
    }
}
