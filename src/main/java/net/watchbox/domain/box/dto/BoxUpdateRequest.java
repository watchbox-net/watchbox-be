package net.watchbox.domain.box.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.VisibleType;

@Getter
@ToString
public class BoxUpdateRequest {
    @Schema(description = "박스 이름", example = "박스2")
    private String name;

    @Schema(description = "박스 설명", example = "시리즈 박스")
    private String description;

    @Schema(description = "공개 여부", allowableValues = {"PRIVATE", "PUBLIC"}, defaultValue = "PRIVATE")
    private VisibleType visibleType;
}