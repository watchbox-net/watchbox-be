package net.watchbox.domain.box.repository.history;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.history.BoxHistorySortOrder;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.history.BoxHistory;
import net.watchbox.domain.box.entity.history.QBoxHistory;
import net.watchbox.domain.content.entity.QContent;
import net.watchbox.domain.content.sub.movie.entity.QMovie;
import net.watchbox.domain.content.sub.person.entity.QPerson;
import net.watchbox.domain.content.sub.tv.entity.QTv;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class BoxHistoryQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 박스 단위 커서 페이지네이션 - size + 1 개를 가져와서 hasNext 판단은 호출 측에서.
     * append-only 로그라 boxHistoryId 가 createdAt 과 단조 증가 → id 단독으로 커서/정렬 가능.
     */
    public List<BoxHistory> findBoxHistoryList(Box box, BoxHistorySortOrder sort, Long cursorId, int size) {
        QBoxHistory history = QBoxHistory.boxHistory;
        QContent content = QContent.content;

        BooleanBuilder conditions = new BooleanBuilder()
                .and(history.box.eq(box))
                .and(cursorCondition(sort, cursorId)); // null 이면 무시됨 (첫 페이지)

        return jpaQueryFactory
                .selectFrom(history)
                .leftJoin(history.actor).fetchJoin()
                .leftJoin(history.targetMember).fetchJoin()
                .leftJoin(history.content, content).fetchJoin()
                .leftJoin(content.movie, QMovie.movie).fetchJoin()
                .leftJoin(content.tv, QTv.tv).fetchJoin()
                .leftJoin(content.person, QPerson.person).fetchJoin()
                .where(conditions)
                .orderBy(orderSpecifier(sort))
                .limit(size + 1L) // hasNext 판단용 +1
                .fetch();
    }

    private BooleanExpression cursorCondition(BoxHistorySortOrder sort, Long cursorId) {
        if (cursorId == null) return null;
        QBoxHistory h = QBoxHistory.boxHistory;
        return switch (sort) {
            case RECENT -> h.boxHistoryId.lt(cursorId);
            case OLDEST -> h.boxHistoryId.gt(cursorId);
        };
    }

    private OrderSpecifier<?> orderSpecifier(BoxHistorySortOrder sort) {
        QBoxHistory h = QBoxHistory.boxHistory;
        return switch (sort) {
            case RECENT -> h.boxHistoryId.desc();
            case OLDEST -> h.boxHistoryId.asc();
        };
    }
}
