package net.watchbox.domain.box.entity.content;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.BoxMember;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class SharedContent extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedContentId;

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
