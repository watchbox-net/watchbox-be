package net.watchbox.domain.record.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.QContent;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.request.WatchMediaTypeFilter;
import net.watchbox.domain.record.dto.request.WatchRecordFilter;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.entity.QContentRecord;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.watchbox.global.util.QuerydslRepositoryUtil.getOrderSpecifiersBySort;

@RequiredArgsConstructor
@Repository
public class ContentRecordQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<ContentRecord> findMyContentRecordList(Member member, ContentRecordQueryRequest request) {
        QContentRecord contentRecord = QContentRecord.contentRecord;
        QContent content = QContent.content;
        QMovie movie = QMovie.movie;
        QTv tv = QTv.tv;

        // 정렬
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiersBySort(request.getSort());

        // 필터
        WatchMediaTypeFilter watchMediaTypeFilter = request.getWatchMediaTypeFilter();
        BooleanBuilder conditions = new BooleanBuilder()
                .and(contentRecord.member.eq(member))
                .and(watchMediaTypeFilterCondition(watchMediaTypeFilter))
                .and(watchRecordFilterCondition(request.getWatchRecordFilter()));

        // ContentRecord 쿼리
        JPAQuery<ContentRecord> contentRecordQuery = jpaQueryFactory
                .selectFrom(contentRecord)
                .leftJoin(contentRecord.content, content).fetchJoin();

        // WatchMediaType에 따라 필요한 SubContent만 fetch join
        applyWatchMediaTypeFetchJoin(contentRecordQuery, content, watchMediaTypeFilter);

        return contentRecordQuery
                .where(conditions)
                .orderBy(orderSpecifiers)
                .fetch();
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
                ? null // 전체(영화+TV) → 조건 없음
                : cr.mediaType.eq(filter.toMediaType());
    }

    private BooleanExpression watchRecordFilterCondition(WatchRecordFilter watchRecordFilter) {
        QContentRecord cr = QContentRecord.contentRecord;
        return switch (watchRecordFilter) {
            case ALL -> null; // QueryDSL where 가변인자: null 은 무시되어 조건 없음 = 전체
            case LIKED -> cr.liked.isTrue();
            case COMPLETED, WATCHING, PLANNED, PAUSED ->
                    cr.watchStatus.eq(watchRecordFilter.toWatchStatus());
        };
    }
}
