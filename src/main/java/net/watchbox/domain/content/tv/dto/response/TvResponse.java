package net.watchbox.domain.content.tv.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.tv.entity.Tv;
import net.watchbox.domain.tmdb.response.tvserieslists.TmdbTvSeriesListsResultItem;
import net.watchbox.domain.tmdb.util.TvGenre;

import java.util.List;
import java.util.Objects;

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
    private Integer year; // 처음 방영 연도
    private List<String> genres;
    private String overview;
    private List<String> originCountry; // TMDB API 응답이 단수형

    public static TvResponse from(Tv tv){
        List<String> genreNames = tv.getGenreIds().stream()
                .map(TvGenre::getKoreanNameById)
                .filter(Objects::nonNull)
                .toList();

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
                .genres(genreNames)
                .overview(tv.getOverview())
                .originCountry(tv.getOriginCountry())
                .build();
    }

    public static TvResponse from(TmdbTvSeriesListsResultItem item) {
        List<String> genreNames = item.getGenreIds().stream()
                .map(TvGenre::getKoreanNameById)
                .filter(Objects::nonNull)
                .toList();

        Integer year = null;
        if (item.getFirstAirDate() != null && item.getFirstAirDate().length() >= 4) {
            year = Integer.parseInt(item.getFirstAirDate().substring(0, 4));
        }

        return TvResponse.builder()
                .id(item.getId().longValue())
                .nameKo(item.getName())
                .nameOriginal(item.getOriginalName())
                .posterPath(item.getPosterPath())
                .popularity(item.getPopularity())
                .voteAverage(item.getVoteAverage())
                .voteCount(item.getVoteCount().longValue())
                .year(year)
                .genres(genreNames)
                .overview(item.getOverview())
                .originCountry(item.getOriginCountry())
                .build();
    }
}
