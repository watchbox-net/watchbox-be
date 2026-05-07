package net.watchbox.global.tmdb.response.people;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.watchbox.global.tmdb.response.common.TmdbCombinedCreditsResponse;
import net.watchbox.global.tmdb.response.common.TmdbPersonImagesResponse;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbPersonDetailsResponse {
    private boolean adult;

    private String biography;

    private String birthday;

    private String deathday;

    private Integer gender;

    private String homepage;

    private Long id;

    @JsonProperty("imdb_id")
    private String imdbId;

    @JsonProperty("known_for_department")
    private String knownForDepartment;

    private String name;

    @JsonProperty("place_of_birth")
    private String placeOfBirth;

    private Double popularity;

    @JsonProperty("profile_path")
    private String profilePath;

    @JsonProperty("also_known_as")
    private List<String> alsoKnownAs;

    /**
     * append_to_response
     */
    private TmdbPersonImagesResponse images;

    @JsonProperty("combined_credits")
    private TmdbCombinedCreditsResponse combinedCredits;
}