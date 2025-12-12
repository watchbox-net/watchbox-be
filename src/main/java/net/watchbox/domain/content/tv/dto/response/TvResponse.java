package net.watchbox.domain.content.tv.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.tv.entity.Tv;

import java.util.List;

@Getter
@ToString
@Builder
public class TvResponse {
    private Long id; // tmdbId
    private String nameKo;
    private String nameEn;
    private String nameOriginal;
    private String posterPath;
    private Double popularity;
    private Double voteAverage;
    private Long voteCount;
    private String year; // 처음 방영 연도
    private List<Integer> genreIds;
    private String overview;
    private List<String> originCountry; // TMDB API 응답이 단수형

    public static TvResponse from(Tv tv){
        return TvResponse.builder()
                .id(tv.getTmdbId())
                .nameKo(tv.getNameKo())
                .nameEn(tv.getNameEn())
                .nameOriginal(tv.getNameOriginal())
                .posterPath(tv.getPosterPath())
                .popularity(tv.getPopularity())
                .voteAverage(tv.getVoteAverage())
                .voteCount(tv.getVoteCount())
                .year(tv.getYear())
                .genreIds(tv.getGenreIds())
                .overview(tv.getOverview())
                .originCountry(tv.getOriginCountry())
                .build();
    }
}
