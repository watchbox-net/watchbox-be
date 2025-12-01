package net.watchpeople.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.box.enums.BoxType;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.global.entity.BaseTime;

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

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = true)
    private Member owner; // MyBox 또는 PublicBox 소유자

    @Enumerated(EnumType.STRING)
    private BoxType boxType;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxMember> boxMembers;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxContent> boxContents;

}
