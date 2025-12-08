package net.watchpeople.domain.content.common.entity;

import jakarta.persistence.*;
import net.watchpeople.global.entity.BaseTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "media_type")
@Table(name = "content")
public abstract class Content extends BaseTime {
    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;  // TMDB ID 그대로 사용, shared key

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", updatable = false)
    private MediaType mediaType;
    // @DiscriminatorColumn("MOVIE")와 매핑되어 미디어 타입을 구분

    private boolean isSaved; // 하위 엔티티가 모두 저장되었는지 여부
}
