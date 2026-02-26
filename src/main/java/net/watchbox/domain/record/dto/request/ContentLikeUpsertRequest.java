package net.watchbox.domain.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.base.entity.MediaType;

@Getter
@ToString
public class ContentLikeUpsertRequest {
    @NotNull
    @Schema(description = "tjfasdgsadgsadgasdgasdg", defaultValue = "550")
    private Long contentId;

    @NotNull
    @Schema(defaultValue = "MOVIE")
    private MediaType mediaType;

    @NotNull
    @Schema(defaultValue = "true")
    private Boolean liked;
}
