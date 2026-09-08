package net.watchbox.domain.box.entity.history;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

/**
 * 박스 멤버 활동 히스토리 (append-only 이벤트 로그).
 *
 * <p>설계 원칙
 * <ul>
 *   <li>상태 테이블(box_member 등)이 아니라 <b>영속 식별자 테이블</b>(box, member, content)을 참조 —
 *       멤버가 박스를 나가거나 콘텐츠가 삭제돼도 사건 기록은 보존되도록.</li>
 *   <li>actor / targetMember 는 회원 탈퇴 대비 nullable (FK ON DELETE SET NULL). NULL 이면 "탈퇴한 사용자".</li>
 *   <li>이벤트 타입별로 사용하는 선택 필드가 다름 (content / targetMember / old·newValue).</li>
 * </ul>
 *
 * <p>생성 시점은 {@link BaseTime#getCreatedAt()} 사용 (시간순 정렬 키).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
//@Table(name = "box_history",
//        indexes = @Index(name = "idx_box_created", columnList = "box_id, created_at"))
// 성능 개선으로 복합 인덱스 넣자
public class BoxHistory extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxHistoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private BoxHistoryEventType eventType;

    /** 사건이 발생한 박스. 박스 삭제 시 히스토리도 함께 제거 (ON DELETE CASCADE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    /** 행위 주체. 탈퇴 시 NULL ("탈퇴한 사용자"). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_member_id")
    private Member actor;

    /** MEMBER_JOINED / MEMBER_LEFT / MEMBER_KICKED 대상. 그 외엔 NULL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id")
    private Member targetMember;

    /** CONTENT_ADDED / CONTENT_DELETED 대상. 그 외엔 NULL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    /** XX_NAME_CHANGED / BOX_DESCRIPTION_CHANGED 의 변경 전 값. */
    private String oldValue;

    /** XX_NAME_CHANGED / BOX_DESCRIPTION_CHANGED 의 변경 후 값. */
    private String newValue;

    // ─────────────────── 정적 팩토리 ───────────────────

    /** 콘텐츠 추가/삭제 이벤트. */
    public static BoxHistory ofContentEvent(Box box, Member actor, BoxHistoryEventType eventType, Content content) {
        return BoxHistory.builder()
                .box(box)
                .actor(actor)
                .eventType(eventType)
                .content(content)
                .build();
    }

    /** 멤버 합류/탈퇴/추방 이벤트. */
    public static BoxHistory ofMemberEvent(Box box, Member actor, BoxHistoryEventType eventType, Member targetMember) {
        return BoxHistory.builder()
                .box(box)
                .actor(actor)
                .eventType(eventType)
                .targetMember(targetMember)
                .build();
    }

    /** 박스 이름/설명 변경 이벤트. */
    public static BoxHistory ofValueChange(Box box, Member actor, BoxHistoryEventType eventType,
                                           String oldValue, String newValue) {
        return BoxHistory.builder()
                .box(box)
                .actor(actor)
                .eventType(eventType)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }
}
