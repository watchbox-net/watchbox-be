package net.watchbox.domain.box.dto.box;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.box.VisibleType;

@Getter
@ToString
public class BoxCreateRequest {
    @NotNull
    @Schema(description = "박스 이름", example = "박스1")
    private String name;

    @Schema(description = "박스 설명", example = "영화 박스")
    private String description;

    @Schema(description = "공개 여부", defaultValue = "PRIVATE")
    private VisibleType visibleType = VisibleType.PRIVATE;
}
