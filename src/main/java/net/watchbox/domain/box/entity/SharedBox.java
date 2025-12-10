package net.watchbox.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.box.entity.request.JoinBoxRequest;
import net.watchbox.domain.box.enums.ShareType;
import net.watchbox.global.entity.BaseTime;

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

    @OneToMany(mappedBy = "sharedBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxMember> boxMembers;

    @OneToMany(mappedBy = "sharedBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedBoxContent> sharedBoxContents;

    @OneToMany(mappedBy = "sharedBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JoinBoxRequest> joinBoxRequests;
}
