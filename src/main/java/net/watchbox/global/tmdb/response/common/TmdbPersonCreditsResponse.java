package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.credit.TmdbCastItem;
import net.watchbox.global.tmdb.inner.credit.TmdbCrewItem;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbPersonCreditsResponse {
    private List<TmdbCastItem> cast;
    private List<TmdbCrewItem> crew;
}
