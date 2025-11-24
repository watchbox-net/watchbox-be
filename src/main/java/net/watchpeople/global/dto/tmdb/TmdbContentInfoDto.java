package net.watchpeople.global.dto.tmdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbContentInfoDto {
    private Long id;
    private String title;
    private String originalTitle;
    private String overview;
    private String posterUrl;
    private String backdropUrl;
    private String releaseDate;
    private Double voteAverage;
    private Integer voteCount;
    private Double popularity;
    private String mediaType; // movie or tv
    private String originalLanguage;
}