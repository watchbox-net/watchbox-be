package net.watchbox.domain.record.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.QContent;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.request.WatchRecordFilter;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.entity.QContentRecord;
import net.watchbox.global.dto.request.SortOrder;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.watchbox.global.util.QuerydslRepositoryUtil.getOrderSpecifiersBySort;

@RequiredArgsConstructor
@Repository
public class ContentRecordQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<ContentRecord> findMyContentRecordList(Member member, SortOrder sortOrder, WatchRecordFilter watchRecordFilter) {
        System.out.println("[DEBUG] sortOrder = " + sortOrder);
        QContentRecord contentRecord = QContentRecord.contentRecord;
        QContent content = QContent.content;
        QMovie movie = QMovie.movie;
        QTv tv = QTv.tv;

        // 정렬
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiersBySort(sortOrder);

        // 시청 기록 필터링
        BooleanExpression watchRecordCondition = watchRecordFilterCondition(watchRecordFilter);

        return jpaQueryFactory
                .selectFrom(contentRecord)
                .leftJoin(contentRecord.content, content).fetchJoin()
                .leftJoin(content.movie, movie).fetchJoin()
                .leftJoin(content.tv, tv).fetchJoin()
                .where(
                        contentRecord.member.eq(member),
                        watchRecordCondition
                )
                .orderBy(orderSpecifiers)
                .fetch();
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
