package net.watchbox.domain.content.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.domain.content.sub.person.entity.Person;
import net.watchbox.domain.content.sub.tv.entity.Tv;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Getter
@Table(name = "content", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tmdb_id", "media_type"})
})
public class Content extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private boolean isSaved; // 하위 엔티티가 저장되었는지 여부

    public void markAsSaved() {
        this.isSaved = true;
    }

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private Movie movie;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private Tv tv;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private Person person;

}
