package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.credit.movie.TmdbCastItem;
import net.watchbox.global.tmdb.inner.credit.movie.TmdbCrewItem;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCreditsResponse { // 영화에서의 인물 Credit
    private List<TmdbCastItem> cast;
    private List<TmdbCrewItem> crew;
}
