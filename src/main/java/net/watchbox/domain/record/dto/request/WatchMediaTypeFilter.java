package net.watchbox.domain.record.dto.request;

import net.watchbox.domain.content.entity.MediaType;

public enum WatchMediaTypeFilter {
    MOVIE_TV(null),
    MOVIE(MediaType.MOVIE),
    TV(MediaType.TV);

    private final MediaType mediaType;

    WatchMediaTypeFilter(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaType toMediaType() {
        return mediaType;
    }
}
