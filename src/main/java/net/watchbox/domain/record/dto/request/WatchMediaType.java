package net.watchbox.domain.record.dto.request;

import net.watchbox.domain.content.entity.MediaType;

public enum WatchMediaType {
    MOVIE(MediaType.MOVIE),
    TV(MediaType.TV);

    private final MediaType mediaType;

    WatchMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaType toMediaType() {
        return mediaType;
    }
}
