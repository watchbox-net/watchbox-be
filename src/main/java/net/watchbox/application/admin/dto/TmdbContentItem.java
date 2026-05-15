package net.watchbox.application.admin.dto;

import jakarta.validation.constraints.NotNull;
import net.watchbox.domain.content.entity.MediaType;

public record TmdbContentItem (
    @NotNull Long tmdbId,
    @NotNull MediaType mediaType
){
}
