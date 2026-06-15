package net.watchbox.application.admin.dto;

import jakarta.validation.constraints.NotNull;
import net.watchbox.domain.record.dto.type.WatchMediaType;
import net.watchbox.domain.record.entity.record.WatchStatus;

public record TmdbWatchStatusItem(
        @NotNull Long tmdbId,
        @NotNull WatchMediaType watchMediaType,
        @NotNull WatchStatus watchStatus
) {
}
