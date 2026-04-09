package net.watchbox.domain.content.sub.tv.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class TvDetail {
    @Id
    private Long contentId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "content_id")
    private Tv tv;

    // TMDB List Response
    @Column(columnDefinition = "TEXT")
    private String overview;
    private String backdropPath;
    private String originalLanguage;
    private LocalDate firstAirDate;
    private Boolean adult;
    private Boolean video;

    // TMDB Tv Details
    private String status;
    private String type; // TV 프로그램 유형
    private String tagline;
    private String homepage;
    private Long budget;  // 제작비 (USD)
    private Long revenue;  // 수익 (USD)
    private Integer numberOfEpisodes;
    private Integer numberOfSeasons;
    private LocalDate lastAirDate;

    // JSON으로 저장하는 방식 (나중에 필요해지면 가공)
    @Column(name = "episode_run_time", columnDefinition = "json")
    private String episodeRunTime;  // "[30, 45]"

    @Column(name = "languages", columnDefinition = "json")
    private String languages;  // "['en', 'ko']"

    @Column(name = "origin_country", columnDefinition = "json")
    private String originCountries;  // "['US', 'KR']"

    @Column(name = "created_by", columnDefinition = "json")
    private String createdBy;  // JSON 배열

    @Column(name = "last_episode_to_air", columnDefinition = "json")
    private String lastEpisodeToAir;  // JSON 객체

    @Column(name = "next_episode_to_air", columnDefinition = "json")
    private String nextEpisodeToAir;  // JSON 객체

    @Column(name = "networks", columnDefinition = "json")
    private String networks;  // JSON 배열

    @Column(name = "production_companies", columnDefinition = "json")
    private String productionCompanies;  // JSON 배열

    @Column(name = "production_countries", columnDefinition = "json")
    private String productionCountries;  // JSON 배열

    @Column(name = "spoken_languages", columnDefinition = "json")
    private String spokenLanguages;  // JSON 배열

    @Column(name = "seasons", columnDefinition = "json")
    private String seasons;  // JSON 배열


}
