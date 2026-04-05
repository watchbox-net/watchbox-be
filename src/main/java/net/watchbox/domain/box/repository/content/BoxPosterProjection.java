package net.watchbox.domain.box.repository.content;

import java.time.LocalDateTime;

public interface BoxPosterProjection {
    Long getBoxId();
    String getPosterPath();
    LocalDateTime getCreatedAt(); // Box의 lastContentAddedAt로 사용중
}
