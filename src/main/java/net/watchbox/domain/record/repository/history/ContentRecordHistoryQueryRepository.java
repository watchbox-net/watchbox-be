package net.watchbox.domain.record.repository.history;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.QContent;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.person.entity.QPerson;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.history.ContentRecordHistoryQueryRequest;
import net.watchbox.domain.record.dto.type.ContentRecordHistorySortOrder;
import net.watchbox.domain.record.dto.type.WatchStatusFilter;
import net.watchbox.domain.record.entity.history.ContentRecordHistory;
import net.watchbox.domain.record.entity.history.ContentRecordHistoryEventType;
import net.watchbox.domain.record.entity.history.QContentRecordHistory;
import net.watchbox.global.dto.CursorPayload;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class ContentRecordHistoryQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 커서 페이지네이션 - size + 1 개를 가져와서 hasNext 판단은 호출 측에서.
     * 정렬 키는 createdAt + id (tie-breaker).
     */
    public List<ContentRecordHistory> findMyHistoryList(
            Member member, ContentRecordHistoryQueryRequest request, CursorPayload cursor, int size
    ) {
        QContentRecordHistory history = QContentRecordHistory.contentRecordHistory;
        QContent content = QContent.content;

        ContentRecordHistorySortOrder sort = request.getSort();

        BooleanBuilder conditions = new BooleanBuilder()
                .and(history.member.eq(member))
                .and(watchStatusFilterCondition(request.getWatchStatusFilter()))
                .and(cursorCondition(sort, cursor)); // null 이면 무시됨

        return jpaQueryFactory
                .selectFrom(history)
                .leftJoin(history.content, content).fetchJoin()
                .leftJoin(content.movie, QMovie.movie).fetchJoin()
                .leftJoin(content.tv, QTv.tv).fetchJoin()
                .leftJoin(content.person, QPerson.person).fetchJoin()
                .where(conditions)
                .orderBy(orderSpecifiers(sort))
                .limit(size + 1L) // hasNext 판단용 +1
                .fetch();
    }

    /**
     * 정렬 종류에 맞춘 cursor where 조건 빌더.
     * (createdAt, id) 튜플 비교를 SQL 로 풀어서 작성. cursor null 이면 null (첫 페이지).
     */
    private BooleanExpression cursorCondition(ContentRecordHistorySortOrder sort, CursorPayload cursor) {
        if (cursor == null) return null;
        QContentRecordHistory h = QContentRecordHistory.contentRecordHistory;

        return switch (sort) {
            // (createdAt, id) < (cursor.dateTime, cursor.id)
            case RECENT -> h.createdAt.lt(cursor.dateTime())
                    .or(h.createdAt.eq(cursor.dateTime())
                            .and(h.contentRecordHistoryId.lt(cursor.id())));

            // (createdAt, id) > (cursor.dateTime, cursor.id)
            case OLDEST -> h.createdAt.gt(cursor.dateTime())
                    .or(h.createdAt.eq(cursor.dateTime())
                            .and(h.contentRecordHistoryId.gt(cursor.id())));
        };
    }

    private OrderSpecifier<?>[] orderSpecifiers(ContentRecordHistorySortOrder sort) {
        QContentRecordHistory h = QContentRecordHistory.contentRecordHistory;
        return switch (sort) {
            case RECENT -> new OrderSpecifier<?>[]{h.createdAt.desc(), h.contentRecordHistoryId.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{h.createdAt.asc(), h.contentRecordHistoryId.asc()};
        };
    }

    /**
     * 시청 상태 필터.
     * ALL: 전체 (좋아요/삭제 이벤트 포함). 특정 상태: 해당 상태로 변경된 이벤트만(newStatus 기준).
     */
    private BooleanExpression watchStatusFilterCondition(WatchStatusFilter filter) {
        QContentRecordHistory h = QContentRecordHistory.contentRecordHistory;
        if (filter == WatchStatusFilter.ALL) return null;
        return h.eventType.eq(ContentRecordHistoryEventType.WATCH_STATUS_CHANGED)
                .and(h.newStatus.eq(filter.toWatchStatus()));
    }
}
