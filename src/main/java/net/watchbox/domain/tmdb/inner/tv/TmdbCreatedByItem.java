package net.watchbox.domain.tmdb.inner.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCreatedByItem {
    private Long id;

    @JsonProperty("credit_id")
    private String creditId;

    private String name;

    private Long gender;

    @JsonProperty("profile_path")
    private String profilePath;
}

