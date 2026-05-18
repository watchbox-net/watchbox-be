package net.watchbox.domain.box.repository.content;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.content.BoxContentRecordQueryRequest;
import net.watchbox.domain.box.dto.content.BoxContentSortOrder;
import net.watchbox.domain.box.dto.content.ContentMediaTypeFilter;
import net.watchbox.domain.box.dto.content.WatchStatusFilter;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.entity.content.QBoxContent;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.entity.QContent;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.person.entity.QPerson;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.QContentRecord;
import net.watchbox.global.dto.CursorPayload;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static net.watchbox.global.util.QuerydslRepositoryUtil.getOrderSpecifiersForBoxContent;

@RequiredArgsConstructor
@Repository
public class BoxContentQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 커서 페이지네이션 - size + 1 개를 가져와서 hasNext 판단은 호출 측에서.
     */
    public List<BoxContent> findBoxContentList(
            Box box, Member member, BoxContentRecordQueryRequest request, CursorPayload cursor, int size
    ) {
        QBoxContent boxContent = QBoxContent.boxContent;
        QContent content = QContent.content;
        QContentRecord contentRecord = QContentRecord.contentRecord;

        ContentMediaTypeFilter mediaTypeFilter = request.getContentMediaTypeFilter();
        WatchStatusFilter watchStatusFilter = request.getWatchStatusFilter();

        // 정렬 (PERSON 모드에서 YEAR 정렬 들어오면 RECENT_SAVED 로 fallback)
        BoxContentSortOrder resolvedSort = resolveSortForPerson(request.getSort(), mediaTypeFilter);
        // YEAR 정렬용 date expression - 필터에 따라 join 된 Q엔티티만 참조해야 함
        DateExpression<LocalDate> dateExpr = dateExprFor(mediaTypeFilter);
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiersForBoxContent(resolvedSort, dateExpr);

        // 필터 + cursor 조건 (cursor null 이면 무시됨)
        BooleanBuilder conditions = new BooleanBuilder()
                .and(boxContent.box.eq(box))
                .and(mediaTypeCondition(mediaTypeFilter))
                .and(watchStatusCondition(watchStatusFilter))
                .and(cursorCondition(resolvedSort, dateExpr, cursor));

        // 메인 쿼리
        JPAQuery<BoxContent> boxContentQuery = jpaQueryFactory
                .selectFrom(boxContent)
                .leftJoin(boxContent.content, content).fetchJoin()
                .leftJoin(boxContent.publisher).fetchJoin();

        // MediaType 필터에 따라 SubContent fetch join 분기
        applyMediaTypeFetchJoin(boxContentQuery, content, mediaTypeFilter);

        // ContentRecord LEFT JOIN (member 조건 ON 절) - WatchStatus 필터링용 (fetchJoin X)
        boxContentQuery.leftJoin(contentRecord)
                .on(contentRecord.content.eq(content)
                        .and(contentRecord.member.eq(member)));

        return boxContentQuery
                .where(conditions)
                .orderBy(orderSpecifiers)
                .limit(size + 1L) // hasNext 판단용 +1
                .fetch();
    }

    /**
     * 정렬 종류에 맞춘 cursor where 조건 빌더.
     * 정렬 튜플 비교를 SQL 로 풀어서 작성 (date, createdAt, id).
     * cursor null 이면 null 반환 (첫 페이지).
     */
    private BooleanExpression cursorCondition(
            BoxContentSortOrder sort, DateExpression<LocalDate> dateExpr, CursorPayload cursor
    ) {
        if (cursor == null) return null;
        QBoxContent bc = QBoxContent.boxContent;

        return switch (sort) {
            // (createdAt, id) < (cursor.dateTime, cursor.id)
            case RECENT_SAVED -> bc.createdAt.lt(cursor.dateTime())
                    .or(bc.createdAt.eq(cursor.dateTime())
                            .and(bc.boxContentId.lt(cursor.id())));

            // (createdAt, id) > (cursor.dateTime, cursor.id)
            case OLDEST_SAVED -> bc.createdAt.gt(cursor.dateTime())
                    .or(bc.createdAt.eq(cursor.dateTime())
                            .and(bc.boxContentId.gt(cursor.id())));

            // (date, createdAt, id) < (cursor.date, cursor.dateTime, cursor.id)
            case RECENT_YEAR -> dateExpr.lt(cursor.date())
                    .or(dateExpr.eq(cursor.date())
                            .and(bc.createdAt.lt(cursor.dateTime())))
                    .or(dateExpr.eq(cursor.date())
                            .and(bc.createdAt.eq(cursor.dateTime()))
                            .and(bc.boxContentId.lt(cursor.id())));

            // OLDEST_YEAR: date ASC, createdAt DESC, id DESC (혼합 방향)
            // date > cursor.date  OR  (date == cursor.date AND createdAt < cursor.dateTime)
            //                     OR  (date == cursor.date AND createdAt == cursor.dateTime AND id < cursor.id)
            case OLDEST_YEAR -> dateExpr.gt(cursor.date())
                    .or(dateExpr.eq(cursor.date())
                            .and(bc.createdAt.lt(cursor.dateTime())))
                    .or(dateExpr.eq(cursor.date())
                            .and(bc.createdAt.eq(cursor.dateTime()))
                            .and(bc.boxContentId.lt(cursor.id())));
        };
    }

    // mediaType 필터에 따라 join 된 Q엔티티만 참조하는 date expression 반환
    // Movie.releaseDate / Tv.firstAirDate 기준으로 연월일까지 정확히 정렬
    // PERSON 은 YEAR 정렬 안 들어오므로 (resolveSortForPerson 에서 fallback) 더미로 movie.releaseDate 반환
    private DateExpression<LocalDate> dateExprFor(ContentMediaTypeFilter filter) {
        return switch (filter) {
            case MOVIE -> QMovie.movie.releaseDate;
            case TV -> QTv.tv.firstAirDate;
            case MOVIE_TV -> Expressions.asDate(
                    QMovie.movie.releaseDate.coalesce(QTv.tv.firstAirDate));
            case PERSON -> QMovie.movie.releaseDate; // 사용 안 됨 (RECENT_SAVED 로 fallback)
        };
    }

    // PERSON 필터에 YEAR 정렬 들어오면 RECENT_SAVED 로 fallback (프론트에서 막아도 방어용)
    private BoxContentSortOrder resolveSortForPerson(BoxContentSortOrder sortOrder, ContentMediaTypeFilter filter) {
        if (filter == ContentMediaTypeFilter.PERSON
                && (sortOrder == BoxContentSortOrder.RECENT_YEAR || sortOrder == BoxContentSortOrder.OLDEST_YEAR)) {
            return BoxContentSortOrder.RECENT_SAVED;
        }
        return sortOrder;
    }

    private void applyMediaTypeFetchJoin(JPAQuery<BoxContent> query, QContent content, ContentMediaTypeFilter filter) {
        switch (filter) {
            case MOVIE_TV -> query
                    .leftJoin(content.movie, QMovie.movie).fetchJoin()
                    .leftJoin(content.tv, QTv.tv).fetchJoin();
            case MOVIE -> query.leftJoin(content.movie, QMovie.movie).fetchJoin();
            case TV -> query.leftJoin(content.tv, QTv.tv).fetchJoin();
            case PERSON -> query.leftJoin(content.person, QPerson.person).fetchJoin();
        }
    }

    private BooleanExpression mediaTypeCondition(ContentMediaTypeFilter filter) {
        QContent content = QContent.content;
        return switch (filter) {
            case MOVIE_TV -> content.mediaType.in(MediaType.MOVIE, MediaType.TV);
            case MOVIE -> content.mediaType.eq(MediaType.MOVIE);
            case TV -> content.mediaType.eq(MediaType.TV);
            case PERSON -> content.mediaType.eq(MediaType.PERSON);
        };
    }

    private BooleanExpression watchStatusCondition(WatchStatusFilter filter) {
        QContentRecord cr = QContentRecord.contentRecord;
        return switch (filter) {
            case ALL -> null; // 전체 -> 조건 없음
            case NONE -> cr.isNull(); // ContentRecord 자체 없음
            case COMPLETED, WATCHING, PLANNED, PAUSED ->
                    cr.watchStatus.eq(filter.toWatchStatus());
        };
    }
}
