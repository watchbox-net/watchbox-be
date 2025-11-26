package net.watchpeople.domain.box.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.MediaType;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Box {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxId;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxMember> boxMembers;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxContent> boxContents;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    private BoxType boxType;
}
