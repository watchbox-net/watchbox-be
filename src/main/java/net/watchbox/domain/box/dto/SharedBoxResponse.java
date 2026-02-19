package net.watchbox.domain.box.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.watchbox.domain.box.entity.BoxType;

import java.util.List;

@Data
@AllArgsConstructor
public class SharedBoxResponse {
    private Long boxId;
    private String name;
    private String description;
    private BoxType boxType;

    private List<BoxMemberResponse> members;
}
