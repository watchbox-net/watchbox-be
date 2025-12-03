package net.watchpeople.global.dev;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchpeople.domain.tmdb.dto.search.TmdbContentInfoDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiSearchDevResponseDto {
    private int page;
    private int totalResults;
    private int totalPages;
    private List<TmdbContentInfoDto> results;
}
