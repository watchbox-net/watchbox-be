package net.watchbox.domain.notification.webpush.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

/**
 * 브라우저가 발급받은 Web Push 구독 정보 저장.
 *
 * <p>한 멤버가 여러 디바이스/브라우저에서 구독할 수 있어 (member 1:N subscription).
 * {@code endpoint} 는 브라우저별 unique — 같은 브라우저에서 재구독해도 보통 같은 endpoint.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "push_subscription",
        indexes = @Index(name = "idx_push_sub_member", columnList = "member_id"))
public class PushSubscription extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pushSubscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 푸시 서비스(FCM/APNs/Mozilla) 엔드포인트 URL. */
    @Column(nullable = false, length = 1000, unique = true)
    private String endpoint;

    /** 브라우저 P-256 공개 키 (base64url). */
    @Column(nullable = false, length = 200)
    private String p256dh;

    /** 인증 비밀 (base64url). */
    @Column(nullable = false, length = 100)
    private String authSecret;
}
