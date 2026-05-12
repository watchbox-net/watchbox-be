package net.watchbox.domain.box.entity.member;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class BoxMember extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxMemberId;

    @ManyToOne(fetch = FetchType.LAZY) // 양방향
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private BoxMemberRole role;

    public void changeRole(BoxMemberRole role) {
        this.role = role;
    }
}
