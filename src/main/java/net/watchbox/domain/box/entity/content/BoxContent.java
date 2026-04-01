package net.watchbox.domain.box.entity.content;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.content.base.entity.MediaType;
import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class BoxContent extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boxContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false, unique = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Member publisher;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    public Long getTmdbId() {
        return content.getTmdbId();
    }
}
