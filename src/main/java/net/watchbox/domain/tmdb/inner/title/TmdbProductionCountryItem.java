package net.watchbox.domain.tmdb.inner.title;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbProductionCountryItem {
    @JsonProperty("iso_3166_1")
    private String iso31661;

    private String name;
}

