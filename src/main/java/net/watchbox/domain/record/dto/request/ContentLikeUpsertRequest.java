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
    @Schema(description = "Content ID", defaultValue = "550")
    private Long contentId;

    @NotNull
    @Schema(description = "MOVIE | TV | PERSON", implementation = MediaType.class, defaultValue = "MOVIE")
    private MediaType mediaType;

    @NotNull
    @Schema(description = "Liked or Dislike", defaultValue = "true")
    private Boolean liked;
}
