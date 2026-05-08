package net.watchbox.global.util;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import net.watchbox.domain.box.entity.content.QBoxContent;
import net.watchbox.domain.record.entity.QContentRecord;
import net.watchbox.global.dto.request.SortOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class QuerydslRepositoryUtil {

    private QuerydslRepositoryUtil() {
    }

    public static OrderSpecifier<?>[] getOrderSpecifiersBySort(
            SortOrder sortOrder, DateExpression<LocalDate> dateExpr
    ) {
        return buildOrderSpecifiers(sortOrder, QContentRecord.contentRecord.createdAt, dateExpr);
    }

    public static OrderSpecifier<?>[] getOrderSpecifiersForBoxContent(
            SortOrder sortOrder, DateExpression<LocalDate> dateExpr
    ) {
        return buildOrderSpecifiers(sortOrder, QBoxContent.boxContent.createdAt, dateExpr);
    }

    /**
     * 공통 정렬 OrderSpecifier 빌더.
     *
     * RECENT_YEAR, OLDEST_YEAR 사용 시 호출 측에서
     * 대상 엔티티 -> QContent -> QMovie / QTv 의 LEFT JOIN 이 선행되어야 함.
     * dateExpr 은 호출 측의 mediaType 필터에 맞춰 join 된 Q엔티티만 참조하도록 구성해야 함
     * (join 안 된 Q엔티티를 참조하면 implicit cross join 으로 결과가 비어버림).
     * Movie.releaseDate / Tv.firstAirDate 를 기준으로 연월일까지 정확히 정렬.
     */
    private static OrderSpecifier<?>[] buildOrderSpecifiers(
            SortOrder sortOrder,
            DateTimePath<LocalDateTime> createdAt,
            DateExpression<LocalDate> dateExpr
    ) {
        return switch (sortOrder) {
            case RECENT_SAVED -> new OrderSpecifier<?>[]{
                    createdAt.desc()
            };
            case OLDEST_SAVED -> new OrderSpecifier<?>[]{
                    createdAt.asc()
            };
            case RECENT_YEAR -> new OrderSpecifier<?>[]{
                    dateExpr.desc(),
                    createdAt.desc()
            };
            case OLDEST_YEAR -> new OrderSpecifier<?>[]{
                    dateExpr.asc(),
                    createdAt.desc()
            };
        };
    }
}
