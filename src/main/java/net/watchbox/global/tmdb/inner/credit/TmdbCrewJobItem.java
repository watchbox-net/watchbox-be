package net.watchbox.global.tmdb.inner.credit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCrewJobItem {

    @JsonProperty("credit_id")
    private String creditId;

    private String job;

    @JsonProperty("episode_count")
    private Integer episodeCount;
}
