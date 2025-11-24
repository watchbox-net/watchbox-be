package net.watchpeople.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiSearchRequest {
    private String query;
    private String language = "ko-KR";
    private Integer page = 1;
    private Boolean includeAdult = true;
    private String type = "movie"; // "movie", "tv", or "multi"
}
