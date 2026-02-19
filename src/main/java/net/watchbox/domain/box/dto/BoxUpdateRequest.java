package net.watchbox.domain.box.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.BoxType;

@Getter
@ToString
public class BoxUpdateRequest {
    @NotNull
    @Schema(description = "박스 이름", example = "박스1 수정")
    private String name;

    @Schema(description = "박스 설명", example = "멤버1과 멤버2의 영화 박스 수정")
    private String description;

}
