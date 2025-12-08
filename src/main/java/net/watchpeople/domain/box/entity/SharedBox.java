package net.watchpeople.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.box.entity.content.SharedBoxContent;
import net.watchpeople.domain.box.entity.request.JoinBoxRequest;
import net.watchpeople.domain.box.enums.ShareType;
import net.watchpeople.global.entity.BaseTime;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class SharedBox extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxId;

    @Enumerated(EnumType.STRING)
    private ShareType shareType;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxMember> boxMembers;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedBoxContent> sharedBoxContents;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JoinBoxRequest> joinBoxRequests;
}
