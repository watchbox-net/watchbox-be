package net.watchpeople.domain.content.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.common.Content;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "movie")
@DiscriminatorValue("MOVIE")
public class Movie extends Content {
    /*movie 테이블의 PK도 tmdb_id (media 테이블의 PK와 동일)*/

    private String titleKo;
    private String titleEn;
    private String titleOriginal;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @ElementCollection
    @CollectionTable(name = "movie_genre_ids", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "genre_id")
    private List<Integer> genreIds;

    private String backdropPath;
    private String posterPath;
    private String originalLanguage;
    private Double popularity;
    private LocalDate releaseDate;
    private Double voteAverage;
    private Integer voteCount;
    private Boolean adult;
    private Boolean video;
}