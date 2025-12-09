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
    private List<Integer> genreIds; // ToDo: 변환
    private String posterPath;
    private LocalDate releaseDate;
}
