//package net.watchbox.domain.content.sub.movie.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDate;
//
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Builder
//@Getter
//@Entity
//public class MovieDetail {
//    @Id
//    private Long contentId;
//
//    @OneToOne
//    @MapsId
//    @JoinColumn(name = "content_id")
//    private Movie movie;
//
//    // TMDB List Response
//    @Column(columnDefinition = "TEXT")
//    private String overview;
//    private String backdropPath;
//    private String originalLanguage;
//    private LocalDate releaseDate;
//    private Boolean adult;
//    private Boolean video;
//
//    // TMDB Movie Details
//    private String status;
//    private Integer runtime;  // 상영시간 (분)
//    private String tagline;
//    private String homepage;
//    private String imdbId;  // IMDb ID (예: tt1234567)
//    private Long budget;  // 제작비 (USD)
//    private Long revenue;  // 수익 (USD)
//
//    @Column(name = "production_companies", columnDefinition = "json")
//    private String productionCompanies;  // JSON 배열
//
//    @Column(name = "production_countries", columnDefinition = "json")
//    private String productionCountries;  // JSON 배열
//
//    @Column(name = "spoken_languages", columnDefinition = "json")
//    private String spokenLanguages;  // JSON 배열
//
//
//}
