package net.watchbox.domain.content.tv.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.common.entity.Content;
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
    private String year; // 처음 방영 연도

    @ElementCollection
    @CollectionTable(name = "tv_genre_ids", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "genre_id")
    private List<Integer> genreIds;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @ElementCollection
    @CollectionTable(name = "tv_origin_country", joinColumns = @JoinColumn(name = "tmdb_id"))
    @Column(name = "country_code")
    private List<String> originCountry; // TMDB API 응답이 단수형

    @OneToOne(mappedBy = "tv", cascade = CascadeType.ALL, orphanRemoval = true)
    private TvDetail tvDetail;
}