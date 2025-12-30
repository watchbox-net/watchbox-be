package net.watchbox.domain.box.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharedBoxResponse {
    private Long boxId;
    private String title;
}
