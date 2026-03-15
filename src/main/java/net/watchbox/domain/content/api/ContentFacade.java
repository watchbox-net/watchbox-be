package net.watchbox.domain.content.api;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.detail.ContentInfo;
import net.watchbox.domain.content.base.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.base.mapper.ContentDetailMapper;
import net.watchbox.domain.content.base.service.ContentQueryService;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import static net.watchbox.domain.content.base.entity.MediaType.*;

@Component
@RequiredArgsConstructor
public class ContentFacade {
    private final ContentQueryService contentQueryService;
    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;

    public ContentDetailResponse getContentDetailByDB(MediaType mediaType, Long contentId) {
        ContentInfo contentInfo =
                switch (mediaType) {
                    case MOVIE -> ContentDetailMapper.fromMovie(contentQueryService.getMovieByIdOrThrow(contentId));
                    case TV -> ContentDetailMapper.fromTv(contentQueryService.getTvByIdOrThrow(contentId));
                    case PERSON -> ContentDetailMapper.fromPerson(contentQueryService.getPersonByIdOrThrow(contentId));
                    default -> throw new IllegalArgumentException("Unsupported media type: " + mediaType);
                };

        return null;
    }

    public ContentDetailResponse getContentDetailByTMDB(MediaType mediaType, Long contentId) {
        return null;
    }
}
