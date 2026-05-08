package net.watchbox.global.tmdb.inner.credit.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbAggregateCrewJobItem {

    @JsonProperty("credit_id")
    private String creditId;

    private String job;

    @JsonProperty("episode_count")
    private Integer episodeCount;
}
