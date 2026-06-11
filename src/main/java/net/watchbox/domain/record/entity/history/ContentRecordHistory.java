package net.watchbox.domain.record.entity.history;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.record.WatchStatus;
import net.watchbox.global.entity.BaseTime;

/**
 * 유저의 컨텐츠 시청 기록 변경 히스토리 (append-only 이벤트 로그).
 *
 * <p>설계 원칙
 * <ul>
 *   <li>"현재 상태 테이블"인 {@code content_record} 가 아니라 <b>영속 식별자 테이블</b>(member, content)을 참조 —
 *       content_record 는 계속 update/삭제되므로(=현재 잔액), 히스토리(=거래 내역)는 거기 의존하면 안 됨.</li>
 *   <li>이벤트 타입별로 사용하는 필드가 다름 (status change → old·newStatus / like → 없음).</li>
 * </ul>
 *
 * <p>생성 시점은 {@link BaseTime#getCreatedAt()} 사용 (시간순 정렬 키).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
//@Table(name = "content_record_history",
//        indexes = {
//                @Index(name = "idx_crh_member_created", columnList = "member_id, created_at"),
//                @Index(name = "idx_crh_member_content", columnList = "member_id, content_id")
//        })
public class ContentRecordHistory extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentRecordHistoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ContentRecordHistoryEventType eventType;

    /** 기록 주체. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 대상 컨텐츠 (content_record 가 아니라 content 직접 참조). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    /** WATCH_STATUS_CHANGED 의 변경 전 상태. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WatchStatus oldStatus;

    /** WATCH_STATUS_CHANGED 의 변경 후 상태. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WatchStatus newStatus;

    // ─────────────────── 정적 팩토리 ───────────────────

    /** 시청 상태 변경 이벤트. */
    public static ContentRecordHistory ofStatusChange(Member member, Content content,
                                                      WatchStatus oldStatus, WatchStatus newStatus) {
        return ContentRecordHistory.builder()
                .member(member)
                .content(content)
                .eventType(ContentRecordHistoryEventType.WATCH_STATUS_CHANGED)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();
    }

    /** 좋아요 추가/삭제 이벤트. */
    public static ContentRecordHistory ofLike(Member member, Content content,
                                              ContentRecordHistoryEventType eventType) {
        return ContentRecordHistory.builder()
                .member(member)
                .content(content)
                .eventType(eventType)
                .build();
    }
}
