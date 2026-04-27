package net.watchbox.domain.box.repository.content;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.content.BoxContentRecordQueryRequest;
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
import net.watchbox.global.dto.request.SortOrder;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.watchbox.global.util.QuerydslRepositoryUtil.getOrderSpecifiersForBoxContent;

@RequiredArgsConstructor
@Repository
public class BoxContentQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<BoxContent> findBoxContentList(Box box, Member member, BoxContentRecordQueryRequest request) {
        QBoxContent boxContent = QBoxContent.boxContent;
        QContent content = QContent.content;
        QContentRecord contentRecord = QContentRecord.contentRecord;

        ContentMediaTypeFilter mediaTypeFilter = request.getContentMediaTypeFilter();
        WatchStatusFilter watchStatusFilter = request.getWatchStatusFilter();

        // 정렬 (PERSON 모드에서 year 정렬 들어오면 RECENT_SAVED로 fallback)
        SortOrder resolvedSort = resolveSortForMediaType(request.getSort(), mediaTypeFilter);
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiersForBoxContent(resolvedSort);

        // 필터
        BooleanBuilder conditions = new BooleanBuilder()
                .and(boxContent.box.eq(box))
                .and(mediaTypeCondition(mediaTypeFilter))
                .and(watchStatusCondition(watchStatusFilter));

        // 메인 쿼리
        JPAQuery<BoxContent> query = jpaQueryFactory
                .selectFrom(boxContent)
                .leftJoin(boxContent.content, content).fetchJoin()
                .leftJoin(boxContent.publisher).fetchJoin();

        // MediaType 필터에 따라 SubContent fetch join 분기
        applyMediaTypeFetchJoin(query, content, mediaTypeFilter);

        // ContentRecord LEFT JOIN (member 조건 ON 절) - WatchStatus 필터링용 (fetchJoin X)
        query.leftJoin(contentRecord)
                .on(contentRecord.content.eq(content)
                        .and(contentRecord.member.eq(member)));

        return query
                .where(conditions)
                .orderBy(orderSpecifiers)
                .fetch();
    }

    private SortOrder resolveSortForMediaType(SortOrder sortOrder, ContentMediaTypeFilter filter) {
        if (filter == ContentMediaTypeFilter.PERSON
                && (sortOrder == SortOrder.RECENT_YEAR || sortOrder == SortOrder.OLDEST_YEAR)) {
            return SortOrder.RECENT_SAVED;
        }
        return sortOrder;
    }

    private void applyMediaTypeFetchJoin(JPAQuery<BoxContent> query, QContent content, ContentMediaTypeFilter filter) {
        switch (filter) {
            case MOVIE -> query.leftJoin(content.movie, QMovie.movie).fetchJoin();
            case TV -> query.leftJoin(content.tv, QTv.tv).fetchJoin();
            case PERSON -> query.leftJoin(content.person, QPerson.person).fetchJoin();
            case MOVIE_TV -> query
                    .leftJoin(content.movie, QMovie.movie).fetchJoin()
                    .leftJoin(content.tv, QTv.tv).fetchJoin();
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
