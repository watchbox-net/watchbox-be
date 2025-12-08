package net.watchpeople.domain.content.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.common.Content;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "movie")
@DiscriminatorValue("MOVIE")
public class Movie extends Content {
    // movie 테이블의 PK도 tmdb_id (media 테이블의 PK와 동일)

    private String titleKo;
    private String titleEn;
    private String titleOriginal;
    private String posterPath;
    private Double popularity;
    private Double voteAverage;
    private Integer voteCount;
    private Integer year; // 상영 연도

    @ElementCollection
    @CollectionTable(name = "movie_genre_ids", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "genre_id")
    private List<Integer> genreIds;
}