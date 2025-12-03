package net.watchpeople.domain.content.tv.entity;

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
@Table(name = "tv")
@DiscriminatorValue("TV")
public class Tv extends Content {
    /*tv 테이블의 PK도 tmdb_id*/

    private String nameKo;
    private String nameEn;
    private String nameOriginal;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @ElementCollection
    @CollectionTable(name = "tv_genre_ids", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "genre_id")
    private List<Integer> genreIds;

    @ElementCollection
    @CollectionTable(name = "tv_origin_country", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "country_code")
    private List<String> originCountry; // TMDB API 응답이 단수형

    private String backdropPath;
    private String posterPath;
    private String originalLanguage;
    private Double popularity;
    private LocalDate firstAirDate;
    private Double voteAverage;
    private Integer voteCount;
    private Boolean adult;

}