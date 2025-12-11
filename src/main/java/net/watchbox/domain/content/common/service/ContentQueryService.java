package net.watchbox.domain.content.common.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.repository.ContentRepository;
import net.watchbox.domain.content.movie.entity.Movie;
import net.watchbox.domain.content.movie.repository.MovieRepository;
import net.watchbox.domain.content.person.repository.PersonRepository;
import net.watchbox.domain.content.tv.repository.TvRepository;
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
//                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));
    }

    public Content getContentByIdOrThrow(Long tmdbId) {
        return contentRepository.findById(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));
    }

    public Movie getMovieByIdOrThrow(Long tmdbId) {
        return movieRepository.findById(tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
    }

}
