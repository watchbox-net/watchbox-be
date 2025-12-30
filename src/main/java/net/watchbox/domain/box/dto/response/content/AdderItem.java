package net.watchbox.domain.box.dto.response.content;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class AdderItem {
    private Long id;
    private String nickname;
    private String profileImage;
}
