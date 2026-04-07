package net.watchbox.domain.content.sub.tv.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.global.entity.BaseTime;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "tv")
//@DiscriminatorValue("TV")
public class Tv extends BaseTime {
    @Id
    private Long tmdbId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false, unique = true)
    private Content content;

    private String nameKo;
    private String nameEn;
    private String nameOriginal;
    private String posterPath;
    private Double popularity;
    private Double voteAverage;
    private Long voteCount;
    private Integer year; // 처음 방영 연도

    @ElementCollection
    @CollectionTable(name = "tv_genre_ids", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "genre_id")
    private List<Integer> genreIds;

    @OneToOne(mappedBy = "tv", cascade = CascadeType.ALL, orphanRemoval = true)
    private TvDetail tvDetail;
}