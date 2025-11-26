package net.watchpeople.domain.search.dto.response.list;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import net.watchpeople.domain.content.MediaType;

@Getter
@ToString
@SuperBuilder
public abstract class MultiSearchResponse {
    private Long id;
    private MediaType mediaType;
}
