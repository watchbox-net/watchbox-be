package net.watchbox.domain.box.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.movie.entity.Movie;
import net.watchbox.domain.content.person.entity.Person;
import net.watchbox.domain.content.tv.entity.Tv;

@Getter
@ToString
@Builder
public class BoxContentItem { // 하나의 응답 클래스에 모든 변수 포함 (Union Type 방식)
    private Long contentId;
    private MediaType mediaType;  // MOVIE, TV, PERSON
    private Double popularity;

    // 영화 | TV
    private String posterPath;
    private Double voteAverage;
    private Integer year;

    // 영화 전용
    private String titleKo;

    // TV 전용
    private String nameKo;

    // 인물 전용
    private String knownForDepartment;
    private String profilePath;

    // genre는 변환해야함

    public static BoxContentItem fromMovie(Movie movie) {
        return BoxContentItem.builder()
                .contentId(movie.getTmdbId())
                .mediaType(MediaType.MOVIE)
                .popularity(movie.getPopularity())
                .posterPath(movie.getPosterPath())
                .voteAverage(movie.getVoteAverage())
                .year(movie.getYear())
                .titleKo(movie.getTitleKo())
                .build();
    }

    public static BoxContentItem fromTv(Tv tv) {
        return BoxContentItem.builder()
                .contentId(tv.getTmdbId())
                .mediaType(MediaType.TV)
                .popularity(tv.getPopularity())
                .posterPath(tv.getPosterPath())
                .voteAverage(tv.getVoteAverage())
                .year(tv.getYear())
                .nameKo(tv.getNameKo())
                .build();
    }

    public static BoxContentItem fromPerson(Person person) {
        return BoxContentItem.builder()
                .contentId(person.getTmdbId())
                .mediaType(MediaType.PERSON)
                .popularity(person.getPopularity())
                .nameKo(person.getNameKo())
                .knownForDepartment(person.getKnownForDepartment())
                .profilePath(person.getProfilePath())
                .build();
    }
}
