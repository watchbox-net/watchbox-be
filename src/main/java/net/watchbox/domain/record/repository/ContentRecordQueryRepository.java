package net.watchbox.domain.record.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.QContent;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.request.ContentRecordSortOrder;
import net.watchbox.domain.record.dto.request.WatchMediaTypeFilter;
import net.watchbox.domain.record.dto.request.WatchRecordFilter;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.entity.QContentRecord;
import net.watchbox.global.dto.CursorPayload;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static net.watchbox.global.util.QuerydslRepositoryUtil.getOrderSpecifiersForContentRecord;

@RequiredArgsConstructor
@Repository
public class ContentRecordQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 커서 페이지네이션 - size + 1 개를 가져와서 hasNext 판단은 호출 측에서.
     */
    public List<ContentRecord> findMyContentRecordList(
            Member member, ContentRecordQueryRequest request, CursorPayload cursor, int size
    ) {
        QContentRecord contentRecord = QContentRecord.contentRecord;
        QContent content = QContent.content;

        WatchMediaTypeFilter watchMediaTypeFilter = request.getWatchMediaTypeFilter();
        ContentRecordSortOrder sort = request.getSort();

        // 정렬 - date expression 은 필터에 따라 join 된 Q엔티티만 참조해야 함
        DateExpression<LocalDate> dateExpr = dateExprFor(watchMediaTypeFilter);
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiersForContentRecord(sort, dateExpr);

        BooleanBuilder conditions = new BooleanBuilder()
                .and(contentRecord.member.eq(member))
                .and(watchMediaTypeFilterCondition(watchMediaTypeFilter))
                .and(watchRecordFilterCondition(request.getWatchRecordFilter()))
                .and(cursorCondition(sort, dateExpr, cursor)); // null 이면 무시됨

        JPAQuery<ContentRecord> contentRecordQuery = jpaQueryFactory
                .selectFrom(contentRecord)
                .leftJoin(contentRecord.content, content).fetchJoin();

        applyWatchMediaTypeFetchJoin(contentRecordQuery, content, watchMediaTypeFilter);

        return contentRecordQuery
                .where(conditions)
                .orderBy(orderSpecifiers)
                .limit(size + 1L) // hasNext 판단용 +1
                .fetch();
    }

    /**
     * 정렬 종류에 맞춘 cursor where 조건 빌더.
     * 정렬 튜플 비교를 SQL 로 풀어서 작성 (date, modifiedAt, id).
     * cursor null 이면 null 반환 (첫 페이지).
     */
    private BooleanExpression cursorCondition(
            ContentRecordSortOrder sort, DateExpression<LocalDate> dateExpr, CursorPayload cursor
    ) {
        if (cursor == null) return null;
        QContentRecord cr = QContentRecord.contentRecord;

        return switch (sort) {
            // (modifiedAt, id) < (cursor.dateTime, cursor.id)
            case RECENT_UPDATED -> cr.modifiedAt.lt(cursor.dateTime())
                    .or(cr.modifiedAt.eq(cursor.dateTime())
                            .and(cr.contentRecordId.lt(cursor.id())));

            // (modifiedAt, id) > (cursor.dateTime, cursor.id)
            case OLDEST_UPDATED -> cr.modifiedAt.gt(cursor.dateTime())
                    .or(cr.modifiedAt.eq(cursor.dateTime())
                            .and(cr.contentRecordId.gt(cursor.id())));

            // (date, modifiedAt, id) < (cursor.date, cursor.dateTime, cursor.id)
            case RECENT_YEAR -> dateExpr.lt(cursor.date())
                    .or(dateExpr.eq(cursor.date())
                            .and(cr.modifiedAt.lt(cursor.dateTime())))
                    .or(dateExpr.eq(cursor.date())
                            .and(cr.modifiedAt.eq(cursor.dateTime()))
                            .and(cr.contentRecordId.lt(cursor.id())));

            // OLDEST_YEAR: date ASC, modifiedAt DESC, id DESC (혼합 방향)
            // date > cursor.date  OR  (date == cursor.date AND modifiedAt < cursor.dateTime)
            //                     OR  (date == cursor.date AND modifiedAt == cursor.dateTime AND id < cursor.id)
            case OLDEST_YEAR -> dateExpr.gt(cursor.date())
                    .or(dateExpr.eq(cursor.date())
                            .and(cr.modifiedAt.lt(cursor.dateTime())))
                    .or(dateExpr.eq(cursor.date())
                            .and(cr.modifiedAt.eq(cursor.dateTime()))
                            .and(cr.contentRecordId.lt(cursor.id())));
        };
    }

    // mediaType 필터에 따라 join 된 Q엔티티만 참조하는 date expression 반환
    // Movie.releaseDate / Tv.firstAirDate 기준으로 연월일까지 정확히 정렬
    private DateExpression<LocalDate> dateExprFor(WatchMediaTypeFilter filter) {
        return switch (filter) {
            case MOVIE -> QMovie.movie.releaseDate;
            case TV -> QTv.tv.firstAirDate;
            case MOVIE_TV -> Expressions.asDate(
                    QMovie.movie.releaseDate.coalesce(QTv.tv.firstAirDate));
        };
    }

    private void applyWatchMediaTypeFetchJoin(JPAQuery<ContentRecord> query, QContent content, WatchMediaTypeFilter filter) {
        switch (filter) {
            case MOVIE_TV -> query
                    .leftJoin(content.movie, QMovie.movie).fetchJoin()
                    .leftJoin(content.tv, QTv.tv).fetchJoin();
            case MOVIE -> query.leftJoin(content.movie, QMovie.movie).fetchJoin();
            case TV -> query.leftJoin(content.tv, QTv.tv).fetchJoin();
        }
    }

    private BooleanExpression watchMediaTypeFilterCondition(WatchMediaTypeFilter filter) {
        QContentRecord cr = QContentRecord.contentRecord;
        return filter == WatchMediaTypeFilter.MOVIE_TV
                ? null
                : cr.mediaType.eq(filter.toMediaType());
    }

    private BooleanExpression watchRecordFilterCondition(WatchRecordFilter watchRecordFilter) {
        QContentRecord cr = QContentRecord.contentRecord;
        return switch (watchRecordFilter) {
            case ALL -> null;
            case LIKED -> cr.liked.isTrue();
            case COMPLETED, WATCHING, PLANNED, PAUSED ->
                    cr.watchStatus.eq(watchRecordFilter.toWatchStatus());
        };
    }
}
