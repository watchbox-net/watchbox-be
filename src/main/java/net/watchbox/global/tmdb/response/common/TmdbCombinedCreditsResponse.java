package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.credit.TmdbCombinedCastItem;
import net.watchbox.global.tmdb.inner.credit.TmdbCombinedCrewItem;

import java.util.List;

/**
 * Person 의 combined_credits 응답.
 * 인물의 모든 출연/제작 작품을 영화 + TV 합쳐서 반환.
 * 각 항목의 media_type 필드("movie" / "tv")로 구별.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCombinedCreditsResponse {

    private List<TmdbCombinedCastItem> cast;

    private List<TmdbCombinedCrewItem> crew;
}
