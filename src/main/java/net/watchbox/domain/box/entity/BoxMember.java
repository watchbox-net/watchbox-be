package net.watchbox.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.box.enums.BoxMemberRole;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class BoxMember extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private SharedBox sharedBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "addedBy")
    private List<SharedBoxContent> sharedBoxContents;

    @Enumerated(EnumType.STRING)
    private BoxMemberRole role;
}
