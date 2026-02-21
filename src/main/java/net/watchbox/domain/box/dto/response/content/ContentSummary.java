package net.watchbox.domain.box.dto.response.content;

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
public class ContentSummary { // 하나의 응답 클래스에 모든 변수 포함 (Union Type 방식)
    private Long contentId;
    private MediaType mediaType;  // MOVIE, TV, PERSON
    private Double popularity;

    // 영화 | TV
    private String posterPath;
    private Double voteAverage;
    private Integer year;
    // ToDo: 장르 리스트

    // 영화 전용
    private String title;

    // TV 전용
    private String name;

    // 인물 전용
    private String knownForDepartment;
    private String profilePath;

    // genre는 변환해야함

    public static ContentSummary fromMovie(Movie movie) {
        return ContentSummary.builder()
                .contentId(movie.getTmdbId())
                .mediaType(MediaType.MOVIE)
                .popularity(movie.getPopularity())
                .posterPath(movie.getPosterPath())
                .voteAverage(movie.getVoteAverage())
                .year(movie.getYear())
                .title(movie.getTitleKo())
                .build();
    }

    public static ContentSummary fromTv(Tv tv) {
        return ContentSummary.builder()
                .contentId(tv.getTmdbId())
                .mediaType(MediaType.TV)
                .popularity(tv.getPopularity())
                .posterPath(tv.getPosterPath())
                .voteAverage(tv.getVoteAverage())
                .year(tv.getYear())
                .name(tv.getNameKo())
                .build();
    }

    public static ContentSummary fromPerson(Person person) {
        return ContentSummary.builder()
                .contentId(person.getTmdbId())
                .mediaType(MediaType.PERSON)
                .popularity(person.getPopularity())
                .name(person.getNameKo())
                .knownForDepartment(person.getKnownForDepartment())
                .profilePath(person.getProfilePath())
                .build();
    }
}
