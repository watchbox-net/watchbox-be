package net.watchbox.domain.box.repository.content;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoxPosterDto {
    private Long boxId;
    private String posterPath;
    private LocalDateTime createdAt;
}
