package net.watchbox.global.util;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateExpression;
import net.watchbox.domain.box.dto.content.BoxContentSortOrder;
import net.watchbox.domain.box.entity.content.QBoxContent;
import net.watchbox.domain.record.dto.request.ContentRecordSortOrder;
import net.watchbox.domain.record.entity.QContentRecord;

import java.time.LocalDate;

public class QuerydslRepositoryUtil {

    private QuerydslRepositoryUtil() {
    }

    /**
     * ContentRecord 정렬: 최근/오래된 저장순은 modifiedAt 기준 (최근 수정순).
     * 연도순 정렬은 dateExpr 기준, 동률 시 modifiedAt 으로 tie-break.
     *
     * RECENT_YEAR, OLDEST_YEAR 사용 시 호출 측에서
     * 대상 엔티티 -> QContent -> QMovie / QTv 의 LEFT JOIN 이 선행되어야 함.
     * dateExpr 은 mediaType 필터에 맞춰 join 된 Q엔티티만 참조하도록 구성해야 함
     * (join 안 된 Q엔티티를 참조하면 implicit cross join 으로 결과가 비어버림).
     */
    public static OrderSpecifier<?>[] getOrderSpecifiersForContentRecord(
            ContentRecordSortOrder sortOrder, DateExpression<LocalDate> dateExpr
    ) {
        QContentRecord cr = QContentRecord.contentRecord;
        // 모든 정렬에 PK tie-breaker 추가 - 동일 값에서 페이지 경계 안정성 확보 (커서 페이지네이션 필수)
        return switch (sortOrder) {
            case RECENT_UPDATED -> new OrderSpecifier<?>[]{
                    cr.modifiedAt.desc(),
                    cr.contentRecordId.desc()
            };
            case OLDEST_UPDATED -> new OrderSpecifier<?>[]{
                    cr.modifiedAt.asc(),
                    cr.contentRecordId.asc()
            };
            case RECENT_YEAR -> new OrderSpecifier<?>[]{
                    dateExpr.desc(),
                    cr.modifiedAt.desc(),
                    cr.contentRecordId.desc()
            };
            case OLDEST_YEAR -> new OrderSpecifier<?>[]{
                    dateExpr.asc(),
                    cr.modifiedAt.desc(),
                    cr.contentRecordId.desc()
            };
        };
    }

    /**
     * BoxContent 정렬: 박스에 추가된 시점(createdAt) 기준.
     * 연도순 정렬은 dateExpr 기준, 동률 시 createdAt 으로 tie-break.
     *
     * RECENT_YEAR, OLDEST_YEAR 사용 시 호출 측에서
     * 대상 엔티티 -> QContent -> QMovie / QTv 의 LEFT JOIN 이 선행되어야 함.
     * dateExpr 은 mediaType 필터에 맞춰 join 된 Q엔티티만 참조하도록 구성해야 함.
     */
    public static OrderSpecifier<?>[] getOrderSpecifiersForBoxContent(
            BoxContentSortOrder sortOrder, DateExpression<LocalDate> dateExpr
    ) {
        QBoxContent bc = QBoxContent.boxContent;
        return switch (sortOrder) {
            case RECENT_SAVED -> new OrderSpecifier<?>[]{ bc.createdAt.desc() };
            case OLDEST_SAVED -> new OrderSpecifier<?>[]{ bc.createdAt.asc() };
            case RECENT_YEAR  -> new OrderSpecifier<?>[]{ dateExpr.desc(), bc.createdAt.desc() };
            case OLDEST_YEAR  -> new OrderSpecifier<?>[]{ dateExpr.asc(),  bc.createdAt.desc() };
        };
    }
}
