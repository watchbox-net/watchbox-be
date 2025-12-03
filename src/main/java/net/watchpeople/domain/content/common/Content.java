package net.watchpeople.domain.content.common;

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
    @Column(name = "media_type", insertable = false, updatable = false)
    private MediaType mediaType;
    // insertable = false, updatable = false: 자식 엔티티에서 관리하므로 여기서는 읽기 전용
    // @DiscriminatorColumn("MOVIE")와 매핑되어 미디어 타입을 구분
}
