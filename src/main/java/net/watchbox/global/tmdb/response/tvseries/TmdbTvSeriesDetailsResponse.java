package net.watchbox.global.tmdb.response.tvseries;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.tv.TmdbSeasonItem;
import net.watchbox.global.tmdb.inner.work.TmdbGenreItem;
import net.watchbox.global.tmdb.response.common.TmdbAggregateCreditsResponse;
import net.watchbox.global.tmdb.response.common.TmdbVideosResponse;
import net.watchbox.global.tmdb.response.common.TmdbWatchProvidersResponse;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbTvSeriesDetailsResponse {
    private boolean adult;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    private List<TmdbGenreItem> genres;

    private String homepage;

    private Long id;

    @JsonProperty("in_production")
    private boolean inProduction;

    @JsonProperty("last_air_date")
    private String lastAirDate;

    private String name;

    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;

    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("original_name")
    private String originalName;

    private String overview;

    private Double popularity;

    @JsonProperty("poster_path")
    private String posterPath;

    private String status;

    private String tagline;

    private String type;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Long voteCount;

    private List<String> languages;

    @JsonProperty("origin_country")
    private List<String> originCountry;

//    @JsonProperty("created_by")
//    private List<TmdbCreatedByItem> createdBy;

//    @JsonProperty("last_episode_to_air")
//    private Object lastEpisodeToAir;

//    @JsonProperty("next_episode_to_air")
//    private Object nextEpisodeToAir;

//    private List<TmdbNetworkItem> networks;

//    @JsonProperty("production_companies")
//    private List<TmdbProductionCompanyItem> productionCompanies;
//
//    @JsonProperty("production_countries")
//    private List<TmdbProductionCountryItem> productionCountries;

    private List<TmdbSeasonItem> seasons;

//    @JsonProperty("spoken_languages")
//    private List<TmdbSpokenLanguageItem> spokenLanguages;

    /**
     * append_to_response
     */
    @JsonProperty("aggregate_credits")
    private TmdbAggregateCreditsResponse aggregateCredits;

    private TmdbVideosResponse videos;

    @JsonProperty("watch/providers")
    private TmdbWatchProvidersResponse watchProviders;
}
