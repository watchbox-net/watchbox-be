package net.watchbox.domain.record.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ContentRecordQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;
}
