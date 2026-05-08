package net.watchbox.global.tmdb.inner.watchprovider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCountryProviders {
    private String link;

    private List<TmdbWatchProviderItem> flatrate;

    private List<TmdbWatchProviderItem> buy;

    private List<TmdbWatchProviderItem> rent;
}
