package net.watchbox.domain.box.entity.request;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class BoxJoinRequest extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    public void updateStatus(RequestStatus status) {
        this.status = status;
    }
}
