package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.credit.person.TmdbCombinedCastItem;
import net.watchbox.global.tmdb.inner.credit.person.TmdbCombinedCrewItem;

import java.util.List;

/**
 * Person 의 combined_credits 응답.
 * 인물의 모든 출연/제작 작품을 영화 + TV 합쳐서 반환.
 * 각 항목의 media_type 필드("movie" / "tv")로 구별.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCombinedCreditsResponse { // 인물에서의 작품(영화+TV) credit
    private List<TmdbCombinedCastItem> cast;
    private List<TmdbCombinedCrewItem> crew;
}
