package net.watchbox.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.request.BoxInvitation;
import net.watchbox.domain.box.entity.request.BoxJoinRequest;
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

    private String description; // 설명은 선택 사항

    @Enumerated(EnumType.STRING)
    private VisibleType visibleType; // 공유 박스일 경우에만 공개 가능

    @Enumerated(EnumType.STRING)
    private BoxType boxType;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxMember> boxMembers;

    @OneToMany(mappedBy = "box", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoxInvitation> boxInvitations;

    @OneToMany(mappedBy = "box", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoxJoinRequest> boxJoinRequests;

    public void updateAutoTitle(List<String> memberNames){
        this.name = String.join("와 ", memberNames) + "의 공유 박스";
    }

    public void update(String name, String description, VisibleType visibleType) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
        if (description != null) { // 설명은 비어있을 수 있음
            this.description = description;
        }
        if (visibleType != null && this.boxType == BoxType.SHARED) {
            this.visibleType = visibleType;
        }
    }
}
