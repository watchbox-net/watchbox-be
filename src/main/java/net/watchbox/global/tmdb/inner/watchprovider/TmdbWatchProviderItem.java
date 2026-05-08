package net.watchbox.global.tmdb.inner.watchprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbWatchProviderItem {
    @JsonProperty("logo_path")
    private String logoPath;

    @JsonProperty("provider_id")
    private Long providerId;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("display_priority")
    private Integer displayPriority;
}
