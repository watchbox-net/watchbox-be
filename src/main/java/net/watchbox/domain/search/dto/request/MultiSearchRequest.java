package net.watchbox.domain.search.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiSearchRequest {
    private String query;
    private String type = "movie"; // "movie", "tv", "person", "multi"
    private String language = "ko-KR";
    private Integer page = 1;
    private Boolean includeAdult = true;
}
