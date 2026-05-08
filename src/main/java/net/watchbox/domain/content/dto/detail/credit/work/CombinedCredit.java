package net.watchbox.domain.content.dto.detail.credit.work;

import java.time.LocalDate;

public sealed interface CombinedCredit permits MovieCredit, TvCredit {
    /**
     * 최신순 정렬에 사용할 날짜.
     * MovieCredit -> releaseDate, TvCredit -> firstAirDate
     */
    LocalDate getSortDate();
}
