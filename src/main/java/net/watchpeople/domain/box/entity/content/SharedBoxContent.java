package net.watchpeople.domain.box.entity.content;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.box.entity.SharedBox;
import net.watchpeople.domain.box.entity.BoxMember;
import net.watchpeople.domain.content.common.MediaType;
import net.watchpeople.domain.content.common.Content;
import net.watchpeople.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class SharedBoxContent extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private SharedBox sharedBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false, unique = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id", nullable = false)
    private BoxMember addedBy;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

}
