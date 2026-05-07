package net.watchbox.global.tmdb.inner.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbImageItem {
    @JsonProperty("aspect_ratio")
    private Double aspectRatio;

    private Integer height;

    private Integer width;

    @JsonProperty("iso_3166_1")
    private String iso31661;

    @JsonProperty("iso_639_1")
    private String iso6391;

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Long voteCount;
}
