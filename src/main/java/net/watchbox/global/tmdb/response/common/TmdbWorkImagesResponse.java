package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.image.TmdbImageItem;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbWorkImagesResponse {
    private Long id;
    private List<TmdbImageItem> backdrops;
    private List<TmdbImageItem> logos;
    private List<TmdbImageItem> posters;
}
