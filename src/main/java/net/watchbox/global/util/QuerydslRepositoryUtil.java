package net.watchbox.global.util;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import net.watchbox.domain.record.entity.QContentRecord;
import net.watchbox.global.dto.request.SortOrder;

public class QuerydslRepositoryUtil {

    private QuerydslRepositoryUtil() {
    }

    /**
     * ContentRecord 정렬 기준 OrderSpecifier 배열 반환.
     *
     * RECENT_YEAR, OLDEST_YEAR 사용 시 호출 측에서
     * QContentRecord -> QContent -> QMovie / QTv 의 LEFT JOIN 이 선행되어야 함.
     * 시청기록은 인물 미포함이므로 movie.year / tv.year 만 사용.
     */
    public static OrderSpecifier<?>[] getOrderSpecifiersBySort(SortOrder sortOrder) {
        QContentRecord cr = QContentRecord.contentRecord;
        QMovie m = QMovie.movie;
        QTv t = QTv.tv;

        NumberExpression<Integer> yearExpr = m.year.coalesce(t.year);

        return switch (sortOrder) {
            case RECENT_SAVED -> new OrderSpecifier<?>[]{
                    cr.createdAt.desc()
            };
            case OLDEST_SAVED -> new OrderSpecifier<?>[]{
                    cr.createdAt.asc()
            };
            case RECENT_YEAR -> new OrderSpecifier<?>[]{
                    yearExpr.desc(),
                    cr.createdAt.desc()
            };
            case OLDEST_YEAR -> new OrderSpecifier<?>[]{
                    yearExpr.asc(),
                    cr.createdAt.desc()
            };
        };
    }
}
