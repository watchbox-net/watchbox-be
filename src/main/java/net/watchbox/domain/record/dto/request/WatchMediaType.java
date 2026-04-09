package net.watchbox.domain.record.dto.request;

import net.watchbox.domain.content.entity.MediaType;

public enum WatchMediaType {
    MOVIE,
    TV;

    public MediaType toMediaType() {
        return switch (this) {
            case MOVIE -> MediaType.MOVIE;
            case TV -> MediaType.TV;
        };
    }
}
