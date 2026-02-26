package net.watchbox.domain.content.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.global.entity.BaseTime;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "movie")
//@DiscriminatorValue("MOVIE")
public class Movie extends BaseTime {
    @Id
    private Long tmdbId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false, unique = true)
    private Content content;

    private String titleKo;
    private String titleEn;
    private String titleOriginal;
    private String posterPath;
    private Double popularity;
    private Double voteAverage;
    private Long voteCount;
    private Integer year; // 상영 연도

    @ElementCollection
    @CollectionTable(name = "movie_genre_ids", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "genre_id")
    private List<Integer> genreIds;

    @OneToOne(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private MovieDetail movieDetail;
}