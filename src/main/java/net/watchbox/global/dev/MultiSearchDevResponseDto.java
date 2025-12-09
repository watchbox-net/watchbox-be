package net.watchbox.global.dev;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.domain.tmdb.dto.search.TmdbContentInfoDto;

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
