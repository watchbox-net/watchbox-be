package net.watchbox.domain.content.api;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.base.dto.detail.DataSourceParam;
import net.watchbox.domain.content.base.service.ContentQueryService;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentFacade {
    private final ContentQueryService contentQueryService;
    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;

    public ContentDetailResponse getContentDetailByDB(MediaType mediaType, Long contentId, DataSourceParam source) {
        return null;
    }

    public ContentDetailResponse getContentDetailByTMDB(MediaType mediaType, Long contentId, DataSourceParam source) {
        return null;
    }
}
