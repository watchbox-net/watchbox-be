package net.watchbox.global.dev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevMultiSearchResponseDto {
    private int page;
    private int totalResults;
    private int totalPages;
    private List<DevTmdbContentInfoDto> results;
}
