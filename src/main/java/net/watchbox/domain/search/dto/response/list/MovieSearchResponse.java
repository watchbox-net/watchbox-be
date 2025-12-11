package net.watchbox.domain.search.dto.response.list;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@SuperBuilder
public class MovieSearchResponse extends MultiSearchResponse {
    private String title;
    private String originalTitle;
    private String overview;
    private List<Integer> genreIds;
    private String posterPath;
    private Integer year;
    private LocalDate releaseDate;
}
