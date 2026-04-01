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
    @Schema(defaultValue = "550")
    private Long contentId;

    @NotNull
    @Schema(defaultValue = "MOVIE")
    private WatchMediaType watchMediaType;

    @NotNull
    @Schema(defaultValue = "COMPLETED")
    private WatchStatus watchStatus;
}
