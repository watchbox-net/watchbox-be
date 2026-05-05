package net.watchbox.global.util;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberExpression;
import net.watchbox.domain.box.entity.content.QBoxContent;
import net.watchbox.domain.record.entity.QContentRecord;
import net.watchbox.global.dto.request.SortOrder;

import java.time.LocalDateTime;

public class QuerydslRepositoryUtil {

    private QuerydslRepositoryUtil() {
    }

    public static OrderSpecifier<?>[] getOrderSpecifiersBySort(
            SortOrder sortOrder, NumberExpression<Integer> yearExpr
    ) {
        return buildOrderSpecifiers(sortOrder, QContentRecord.contentRecord.createdAt, yearExpr);
    }

    public static OrderSpecifier<?>[] getOrderSpecifiersForBoxContent(
            SortOrder sortOrder, NumberExpression<Integer> yearExpr
    ) {
        return buildOrderSpecifiers(sortOrder, QBoxContent.boxContent.createdAt, yearExpr);
    }

    /**
     * 공통 정렬 OrderSpecifier 빌더.
     *
     * RECENT_YEAR, OLDEST_YEAR 사용 시 호출 측에서
     * 대상 엔티티 -> QContent -> QMovie / QTv 의 LEFT JOIN 이 선행되어야 함.
     * yearExpr 은 호출 측의 mediaType 필터에 맞춰 join 된 Q엔티티만 참조하도록 구성해야 함
     * (join 안 된 Q엔티티를 참조하면 implicit cross join 으로 결과가 비어버림).
     */
    private static OrderSpecifier<?>[] buildOrderSpecifiers(
            SortOrder sortOrder,
            DateTimePath<LocalDateTime> createdAt,
            NumberExpression<Integer> yearExpr
    ) {
        return switch (sortOrder) {
            case RECENT_SAVED -> new OrderSpecifier<?>[]{
                    createdAt.desc()
            };
            case OLDEST_SAVED -> new OrderSpecifier<?>[]{
                    createdAt.asc()
            };
            case RECENT_YEAR -> new OrderSpecifier<?>[]{
                    yearExpr.desc(),
                    createdAt.desc()
            };
            case OLDEST_YEAR -> new OrderSpecifier<?>[]{
                    yearExpr.asc(),
                    createdAt.desc()
            };
        };
    }

    /**
     * ContentRecord 정렬 기준 OrderSpecifier 배열 반환.
     *
     * RECENT_YEAR, OLDEST_YEAR 사용 시 호출 측에서
     * QContentRecord -> QContent -> QMovie / QTv 의 LEFT JOIN 이 선행되어야 함.
     * 시청기록은 인물 미포함이므로 movie.year / tv.year 만 사용.
     */
//    public static OrderSpecifier<?>[] getOrderSpecifiersBySort(SortOrder sortOrder) {
//        QContentRecord cr = QContentRecord.contentRecord;
//        QMovie m = QMovie.movie;
//        QTv t = QTv.tv;
//
//        NumberExpression<Integer> yearExpr = m.year.coalesce(t.year);
//
//        return switch (sortOrder) {
//            case RECENT_SAVED -> new OrderSpecifier<?>[]{
//                    cr.createdAt.desc()
//            };
//            case OLDEST_SAVED -> new OrderSpecifier<?>[]{
//                    cr.createdAt.asc()
//            };
//            case RECENT_YEAR -> new OrderSpecifier<?>[]{
//                    yearExpr.desc(),
//                    cr.createdAt.desc()
//            };
//            case OLDEST_YEAR -> new OrderSpecifier<?>[]{
//                    yearExpr.asc(),
//                    cr.createdAt.desc()
//            };
//        };
//    }

    /**
     * BoxContent 정렬 기준 OrderSpecifier 배열 반환.
     *
     * RECENT_YEAR, OLDEST_YEAR 사용 시 호출 측에서
     * QBoxContent -> QContent -> QMovie / QTv 의 LEFT JOIN 이 선행되어야 함.
     * PERSON 모드에서는 year 정렬이 의미 없으므로 호출 측에서 RECENT_SAVED 등으로 fallback 권장.
     */
//    public static OrderSpecifier<?>[] getOrderSpecifiersForBoxContent(SortOrder sortOrder) {
//        QBoxContent bc = QBoxContent.boxContent;
//        QMovie m = QMovie.movie;
//        QTv t = QTv.tv;
//
//        NumberExpression<Integer> yearExpr = m.year.coalesce(t.year);
//
//        return switch (sortOrder) {
//            case RECENT_SAVED -> new OrderSpecifier<?>[]{
//                    bc.createdAt.desc()
//            };
//            case OLDEST_SAVED -> new OrderSpecifier<?>[]{
//                    bc.createdAt.asc()
//            };
//            case RECENT_YEAR -> new OrderSpecifier<?>[]{
//                    yearExpr.desc(),
//                    bc.createdAt.desc()
//            };
//            case OLDEST_YEAR -> new OrderSpecifier<?>[]{
//                    yearExpr.asc(),
//                    bc.createdAt.desc()
//            };
//        };
//    }
}
