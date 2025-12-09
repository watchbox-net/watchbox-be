package net.watchbox.global.dev;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiSearchDevRequest {
    private String query;
    private String language = "ko-KR";
    private Integer page = 1;
    private Boolean includeAdult = true;
    private String type = "movie"; // "movie", "tv", or "multi"
}
