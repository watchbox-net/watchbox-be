package net.watchbox.domain.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.record.entity.WatchStatus;

@Getter
@ToString
public class WatchStatusUpsertRequest {
    @NotNull
    @Schema(description = "Content ID", defaultValue = "550")
    private Long contentId;

    @NotNull
    @Schema(description = "MOVIE | TV", implementation = WatchMediaType.class, defaultValue = "MOVIE")
    private WatchMediaType watchMediaType;

    @NotNull
    @Schema(description = "WatchStatus", implementation = WatchStatus.class, defaultValue = "COMPLETED")
    private WatchStatus watchStatus;
}
