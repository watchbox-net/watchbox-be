package net.watchpeople.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.box.enums.RequestStatus;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class BoxShareRequest extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    public void updateStatus(RequestStatus status) {
        this.status = status;
    }
}
