package net.watchbox.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.entity.request.JoinBoxRequest;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Box extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner; // 사실상 마이 박스 리스트 조회시에만 사용

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private BoxType boxType;

    @Enumerated(EnumType.STRING)
    private VisibleType visibleType; // 공유 박스일 경우에만 공개 가능

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxMember> boxMembers;

    @OneToMany(mappedBy = "box", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<InviteBoxRequest> inviteBoxRequests;

    @OneToMany(mappedBy = "box", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<JoinBoxRequest> joinBoxRequests;

    public void updateTitle(String name){
        this.name = name;
    }

    public void updateAutoTitle(List<String> memberNames){
        this.name = String.join("와 ", memberNames) + "의 공유 박스";
    }
}
