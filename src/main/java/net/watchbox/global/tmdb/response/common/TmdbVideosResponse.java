package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.video.TmdbVideoItem;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbVideosResponse {
    private List<TmdbVideoItem> results;
}
