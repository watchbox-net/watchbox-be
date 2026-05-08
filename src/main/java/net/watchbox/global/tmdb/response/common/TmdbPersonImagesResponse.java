package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.image.TmdbImageItem;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbPersonImagesResponse {
    private Long id;
    private List<TmdbImageItem> profiles;  // ← TmdbImageItem 재사용
}
