package net.watchbox.domain.content.sub.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.global.entity.BaseTime;
import net.watchbox.global.tmdb.util.Country;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "movie")
public class Movie extends BaseTime {
    @Id
    private Long contentId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(nullable = false)
    private Long tmdbId;

    private String titleKo;
    private String titleEn;
    private String titleOriginal;
    private String posterPath;
    private Double popularity;
    private Double voteAverage;
    private Long voteCount;
//    private Integer year; // 개봉 연도
    private LocalDate releaseDate;
    private Country originCountry;

    @ElementCollection
    @CollectionTable(name = "movie_genre_ids", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "genre_id")
    private List<Integer> genreIds;

    @OneToOne(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private MovieDetail movieDetail;
}