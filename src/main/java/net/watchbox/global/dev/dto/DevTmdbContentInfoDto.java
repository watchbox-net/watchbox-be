package net.watchbox.global.dev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevTmdbContentInfoDto {
    private Long id;
    private String title;
    private String originalTitle;
    private String overview;
    private List<Integer> genreIds;
    private String posterUrl;
    private String backdropUrl;
    private String releaseDate;
    private Double voteAverage;
    private Integer voteCount;
    private Double popularity;
    private String mediaType; // movie or tv
    private String originalLanguage;
}