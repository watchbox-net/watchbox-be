package net.watchbox.global.tmdb.response.movies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.title.TmdbGenreItem;
import net.watchbox.global.tmdb.inner.title.TmdbProductionCompanyItem;
import net.watchbox.global.tmdb.inner.title.TmdbProductionCountryItem;
import net.watchbox.global.tmdb.inner.title.TmdbSpokenLanguageItem;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbMoviesDetailsResponse {
    private boolean adult;

    @JsonProperty("backdrop_path")
    private String backdropPath;

//    @JsonProperty("belongs_to_collection")
//    private Object belongsToCollection;

    private Long budget;

    private List<TmdbGenreItem> genres;

    private String homepage;

    private Long id;

    @JsonProperty("imdb_id")
    private String imdbId;

    @JsonProperty("origin_country")
    private List<String> originCountry;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("original_title")
    private String originalTitle;

    private String overview;

    private Double popularity;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("release_date")
    private String releaseDate;

    private Long revenue;

    private Long runtime;

    private String status;

    private String tagline;

    private String title;

    private boolean video;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Long voteCount;

//    @JsonProperty("production_companies")
//    private List<TmdbProductionCompanyItem> productionCompanies;
//
//    @JsonProperty("production_countries")
//    private List<TmdbProductionCountryItem> productionCountries;
//
//    @JsonProperty("spoken_languages")
//    private List<TmdbSpokenLanguageItem> spokenLanguages;

    /* append_to_response */
}
