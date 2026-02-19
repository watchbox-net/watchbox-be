package net.watchbox.domain.box.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.BoxType;
import net.watchbox.domain.box.entity.VisibleType;

@Getter
@ToString
public class BoxCreateRequest {
    @NotNull
    @Schema(description = "박스 이름", example = "박스1")
    private String name;

    @Schema(description = "박스 설명", example = "멤버1과 멤버2의 영화 박스")
    private String description;

    @Schema(description = "공개 여부", allowableValues = {"PRIVATE", "PUBLIC"}, defaultValue = "PRIVATE")
    private VisibleType visibleType = VisibleType.PRIVATE;

    @NotNull
    @Schema(description = "박스 타입", allowableValues = {"MY", "SHARED"}, defaultValue = "MY")
    private BoxType boxType;
}
