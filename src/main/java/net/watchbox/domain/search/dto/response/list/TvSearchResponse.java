package net.watchbox.domain.search.dto.response.list;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@SuperBuilder
public class TvSearchResponse extends MultiSearchResponse {
    private String name;
    private String originalName;
    private String overview;
    private List<Integer> genreIds; // ToDo: 변환
    private String posterPath;
    private LocalDate firstAirDate;
}
