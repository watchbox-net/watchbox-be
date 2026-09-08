package net.watchbox.domain.box.repository.content;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.content.request.BoxContentCountRequest;
import net.watchbox.domain.box.dto.content.request.BoxContentQueryRequest;
import net.watchbox.domain.box.dto.content.type.BoxContentSortOrder;
import net.watchbox.domain.box.dto.content.type.ContentMediaTypeFilter;
import net.watchbox.domain.box.dto.content.type.WatchStatusFilter;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.entity.content.QBoxContent;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.entity.QContent;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.person.entity.QPerson;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.record.QContentRecord;
import net.watchbox.global.dto.CursorPayload;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class BoxContentQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 박스 콘텐츠 페이지 조회 — content_id 단위 페이지네이션.
     *
     * 2단계 쿼리:
     *  1) contentId 페이지 선정: GROUP BY content_id, 정렬 = MAX(createdAt) 기준 (가장 최근 추가한 시점)
     *  2) 그 contentId 들에 해당하는 모든 BoxContent fetch (publisher 누락 없음)
     *
     * 같은 content 가 페이지 경계에 걸치거나 page 안에 분산되는 문제 해결.
     * 페이지 내 같은 content 그룹은 publisher 표시 순서를 위해 createdAt ASC 로 정렬.
     */
    public BoxContentPage findBoxContentPage(
            Box box, Member member, BoxContentQueryRequest request, CursorPayload cursor, int size
    ) {
        ContentMediaTypeFilter mediaTypeFilter = request.getContentMediaTypeFilter();
        BoxContentSortOrder resolvedSort = resolveSortForPerson(request.getSort(), mediaTypeFilter);
        DateExpression<LocalDate> dateExpr = dateExprFor(mediaTypeFilter);

        // Step 1: contentId 페이지 (size+1 로 hasNext 판단)
        List<Tuple> idTuples = selectContentIdPage(
                box, member, request.getWatchStatusFilter(), mediaTypeFilter,
                resolvedSort, dateExpr, cursor, size + 1
        );

        if (idTuples.isEmpty()) return BoxContentPage.empty();

        boolean hasNext = idTuples.size() > size;
        List<Tuple> pageTuples = hasNext ? idTuples.subList(0, size) : idTuples;

        List<Long> contentIds = pageTuples.stream()
                .map(t -> t.get(QContent.content.contentId))
                .toList();

        // Step 2: 그 contentIds 의 모든 BoxContent fetch (publisher 포함)
        List<BoxContent> allBoxContents = fetchBoxContentsByContentIds(box, contentIds, mediaTypeFilter);

        // 정렬 복원: Step 1 의 contentId 순서대로 + 그룹 내 createdAt ASC (Spotify 패턴)
        List<BoxContent> ordered = reorderByContentIds(allBoxContents, contentIds);

        // nextCursor 생성 (페이지 마지막 contentId 의 정렬 키)
        CursorPayload nextCursor = hasNext
                ? buildCursorFromTuple(pageTuples.get(pageTuples.size() - 1), resolvedSort, dateExpr)
                : null;

        return new BoxContentPage(ordered, hasNext, nextCursor);
    }

    /** Step 1: GROUP BY content_id + 정렬 + cursor + limit → (contentId, maxCreatedAt, optional date) tuple */
    private List<Tuple> selectContentIdPage(
            Box box, Member member,
            WatchStatusFilter watchStatusFilter, ContentMediaTypeFilter mediaTypeFilter,
            BoxContentSortOrder sort, DateExpression<LocalDate> dateExpr,
            CursorPayload cursor, int limit
    ) {
        QBoxContent boxContent = QBoxContent.boxContent;
        QContent content = QContent.content;
        QContentRecord contentRecord = QContentRecord.contentRecord;

        NumberPath<Long> contentIdPath = content.contentId;
        DateTimeExpression<LocalDateTime> maxCreatedAt = boxContent.createdAt.max();
        boolean isYearSort = sort == BoxContentSortOrder.RECENT_YEAR || sort == BoxContentSortOrder.OLDEST_YEAR;

        // select 절: 항상 contentId + maxCreatedAt, YEAR 정렬 시 dateExpr 추가
        JPAQuery<Tuple> query = isYearSort
                ? jpaQueryFactory.select(contentIdPath, maxCreatedAt, dateExpr)
                : jpaQueryFactory.select(contentIdPath, maxCreatedAt);

        query.from(boxContent)
                .leftJoin(boxContent.content, content);

        // mediaType 필터에 따라 movie/tv join (YEAR 정렬용 dateExpr 평가 위해)
        applyMediaTypeJoinForSelect(query, content, mediaTypeFilter);

        // WatchStatus 필터링용 ContentRecord LEFT JOIN
        query.leftJoin(contentRecord)
                .on(contentRecord.content.eq(content)
                        .and(contentRecord.member.eq(member)));

        BooleanBuilder where = new BooleanBuilder()
                .and(boxContent.box.eq(box))
                .and(mediaTypeCondition(mediaTypeFilter))
                .and(watchStatusCondition(watchStatusFilter));

        // GROUP BY 절
        if (isYearSort) {
            query.groupBy(contentIdPath, dateExpr);
        } else {
            query.groupBy(contentIdPath);
        }

        return query
                .where(where)
                .having(cursorConditionForContent(sort, dateExpr, maxCreatedAt, contentIdPath, cursor))
                .orderBy(orderSpecifiersForContentPage(sort, dateExpr, maxCreatedAt, contentIdPath))
                .limit(limit)
                .fetch();
    }

    /** Step 2: contentIds 의 모든 BoxContent 를 fetch join 으로 한 번에 */
    private List<BoxContent> fetchBoxContentsByContentIds(
            Box box, List<Long> contentIds, ContentMediaTypeFilter mediaTypeFilter
    ) {
        QBoxContent boxContent = QBoxContent.boxContent;
        QContent content = QContent.content;

        JPAQuery<BoxContent> query = jpaQueryFactory
                .selectFrom(boxContent)
                .leftJoin(boxContent.content, content).fetchJoin()
                .leftJoin(boxContent.publisher).fetchJoin();

        applyMediaTypeFetchJoin(query, content, mediaTypeFilter);

        return query
                .where(boxContent.box.eq(box)
                        .and(content.contentId.in(contentIds)))
                .fetch();
    }

    /** Step 2 결과를 contentIds 순서대로 + 그룹 내 createdAt ASC 정렬 */
    private List<BoxContent> reorderByContentIds(List<BoxContent> all, List<Long> contentIdsInOrder) {
        Map<Long, List<BoxContent>> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        bc -> bc.getContent().getContentId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        grouped.values().forEach(g -> g.sort(Comparator.comparing(BoxContent::getCreatedAt)));

        List<BoxContent> ordered = new ArrayList<>();
        for (Long cid : contentIdsInOrder) {
            List<BoxContent> group = grouped.get(cid);
            if (group != null) ordered.addAll(group);
        }
        return ordered;
    }

    /** 페이지 마지막 contentId tuple 로부터 nextCursor 생성 */
    private CursorPayload buildCursorFromTuple(
            Tuple last, BoxContentSortOrder sort, DateExpression<LocalDate> dateExpr
    ) {
        Long contentId = last.get(QContent.content.contentId);
        LocalDateTime maxCreatedAt = last.get(QBoxContent.boxContent.createdAt.max());

        if (sort == BoxContentSortOrder.RECENT_YEAR || sort == BoxContentSortOrder.OLDEST_YEAR) {
            LocalDate date = last.get(dateExpr);
            return CursorPayload.of(date, maxCreatedAt, contentId);
        }
        return CursorPayload.of(maxCreatedAt, contentId);
    }

    /**
     * Step 1 의 HAVING 절 — cursor 기준 (maxCreatedAt, contentId) 튜플 비교.
     * GROUP BY 결과에 조건 거는 거라 HAVING 사용.
     */
    private BooleanExpression cursorConditionForContent(
            BoxContentSortOrder sort, DateExpression<LocalDate> dateExpr,
            DateTimeExpression<LocalDateTime> maxCreatedAt, NumberPath<Long> contentIdPath,
            CursorPayload cursor
    ) {
        if (cursor == null) return null;

        return switch (sort) {
            case RECENT_SAVED -> maxCreatedAt.lt(cursor.dateTime())
                    .or(maxCreatedAt.eq(cursor.dateTime())
                            .and(contentIdPath.lt(cursor.id())));

            case OLDEST_SAVED -> maxCreatedAt.gt(cursor.dateTime())
                    .or(maxCreatedAt.eq(cursor.dateTime())
                            .and(contentIdPath.gt(cursor.id())));

            case RECENT_YEAR -> dateExpr.lt(cursor.date())
                    .or(dateExpr.eq(cursor.date())
                            .and(maxCreatedAt.lt(cursor.dateTime())))
                    .or(dateExpr.eq(cursor.date())
                            .and(maxCreatedAt.eq(cursor.dateTime()))
                            .and(contentIdPath.lt(cursor.id())));

            // OLDEST_YEAR: date ASC, maxCreatedAt DESC, contentId DESC
            case OLDEST_YEAR -> dateExpr.gt(cursor.date())
                    .or(dateExpr.eq(cursor.date())
                            .and(maxCreatedAt.lt(cursor.dateTime())))
                    .or(dateExpr.eq(cursor.date())
                            .and(maxCreatedAt.eq(cursor.dateTime()))
                            .and(contentIdPath.lt(cursor.id())));
        };
    }

    /** Step 1 ORDER BY — content 단위 정렬 키 */
    private OrderSpecifier<?>[] orderSpecifiersForContentPage(
            BoxContentSortOrder sort, DateExpression<LocalDate> dateExpr,
            DateTimeExpression<LocalDateTime> maxCreatedAt, NumberPath<Long> contentIdPath
    ) {
        return switch (sort) {
            case RECENT_SAVED -> new OrderSpecifier<?>[]{
                    maxCreatedAt.desc(),
                    contentIdPath.desc()
            };
            case OLDEST_SAVED -> new OrderSpecifier<?>[]{
                    maxCreatedAt.asc(),
                    contentIdPath.asc()
            };
            case RECENT_YEAR -> new OrderSpecifier<?>[]{
                    dateExpr.desc(),
                    maxCreatedAt.desc(),
                    contentIdPath.desc()
            };
            case OLDEST_YEAR -> new OrderSpecifier<?>[]{
                    dateExpr.asc(),
                    maxCreatedAt.desc(),
                    contentIdPath.desc()
            };
        };
    }

    /** Step 1 select 쿼리용 join — fetchJoin 불가 (GROUP BY 와 충돌) */
    private void applyMediaTypeJoinForSelect(JPAQuery<?> query, QContent content, ContentMediaTypeFilter filter) {
        switch (filter) {
            case MOVIE_TV -> query
                    .leftJoin(content.movie, QMovie.movie)
                    .leftJoin(content.tv, QTv.tv);
            case MOVIE -> query.leftJoin(content.movie, QMovie.movie);
            case TV -> query.leftJoin(content.tv, QTv.tv);
            case PERSON -> query.leftJoin(content.person, QPerson.person);
        }
    }

    /**
     * 박스 + 미디어타입 필터 + 시청상태 필터 적용한 BoxContent 총 개수.
     * content_id distinct 기준 count (같은 content 여러 멤버 추가해도 1로 계산).
     */
    public Long countBoxContent(
            Box box, Member member, BoxContentCountRequest request
    ) {
        QBoxContent boxContent = QBoxContent.boxContent;
        QContent content = QContent.content;
        QContentRecord contentRecord = QContentRecord.contentRecord;

        BooleanBuilder conditions = new BooleanBuilder()
                .and(boxContent.box.eq(box))
                .and(mediaTypeCondition(request.getContentMediaTypeFilter()))
                .and(watchStatusCondition(request.getWatchStatusFilter()));

        Long count = jpaQueryFactory
                .select(content.contentId.countDistinct())
                .from(boxContent)
                .leftJoin(boxContent.content, content)
                .leftJoin(contentRecord)
                .on(contentRecord.content.eq(content)
                        .and(contentRecord.member.eq(member)))
                .where(conditions)
                .fetchOne();

        return count != null ? count : 0L;
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

    /** Step 2 fetch 쿼리용 join (fetchJoin OK) */
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
            case ALL -> null;
            case NONE -> cr.isNull();
            case COMPLETED, WATCHING, PLANNED, PAUSED ->
                    cr.watchStatus.eq(filter.toWatchStatus());
        };
    }
}
