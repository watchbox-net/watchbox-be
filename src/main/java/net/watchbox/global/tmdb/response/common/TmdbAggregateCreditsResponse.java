package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.credit.tv.TmdbAggregateCastItem;
import net.watchbox.global.tmdb.inner.credit.tv.TmdbAggregateCrewItem;

import java.util.List;

/**
 * TV 시리즈 aggregate_credits 응답.
 * 영화의 credits 와 달리 시즌별 누적 (한 인물이 여러 캐릭터/직무 가능).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbAggregateCreditsResponse { // TV 시리즈에서의 인물 Credit (시즌별 누적)
    private List<TmdbAggregateCastItem> cast;
    private List<TmdbAggregateCrewItem> crew;
}
