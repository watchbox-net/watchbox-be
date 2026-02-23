package net.watchbox.domain.content.base.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.movie.entity.Movie;
import net.watchbox.domain.content.person.entity.Person;
import net.watchbox.domain.content.tv.entity.Tv;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Getter
//@Inheritance(strategy = InheritanceType.JOINED)
//@DiscriminatorColumn(name = "media_type")
@Table(name = "content")
public class Content extends BaseTime {
    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;  // TMDB ID 그대로 사용, shared key

    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private boolean isSaved; // 하위 엔티티가 저장되었는지 여부

    public void markAsSaved() {
        this.isSaved = true;
    }

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Movie movie;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Tv tv;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Person person;

}
