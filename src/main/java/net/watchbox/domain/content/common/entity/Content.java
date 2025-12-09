package net.watchbox.domain.content.common.entity;

import jakarta.persistence.*;
import net.watchbox.global.entity.BaseTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "media_type")
@Table(name = "content")
public abstract class Content extends BaseTime {
    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;  // TMDB ID 그대로 사용, shared key

    private boolean isSaved; // 하위 엔티티가 모두 저장되었는지 여부
}
