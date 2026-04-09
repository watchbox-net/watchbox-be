package net.watchbox.domain.content.dto.box;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ContentBoxUpdateResponse {
    private List<Long> addedBoxIds;
    private List<Long> removedBoxIds;
}
