package net.watchbox.domain.content.base.dto.list;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.domain.content.base.entity.MediaType;
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

    // 영화, TV 공통
    private String posterPath;
    private Double voteAverage;
    private Long voteCount;
    private Integer year;
//    private String overview;
//    private List<String> genreNameList; // ToDo: 장르 리스트 변환

    // 영화 전용
    private String title; // titleKo
    private String titleOriginal;

    // TV, 인물 공통
    private String name; // nameKo
    private String nameOriginal;

    // 인물 전용
    private String knownForDepartment;
    private String profilePath;

    public static ContentSummary fromContent(Content content) {
        return switch (content.getMediaType()) {
            case MOVIE -> fromMovie(content.getMovie());
            case TV -> fromTv(content.getTv());
            case PERSON -> fromPerson(content.getPerson());
        };
    }

    public static ContentSummary fromMovie(Movie movie) {
        return ContentSummary.builder()
                .contentId(movie.getTmdbId())
                .mediaType(MediaType.MOVIE)
                .popularity(movie.getPopularity())
                .posterPath(movie.getPosterPath())
                .voteAverage(movie.getVoteAverage())
                .voteCount(movie.getVoteCount())
                .year(movie.getYear())
                .title(movie.getTitleKo())
                .title(movie.getTitleOriginal())
                .build();
    }

    public static ContentSummary fromTv(Tv tv) {
        return ContentSummary.builder()
                .contentId(tv.getTmdbId())
                .mediaType(MediaType.TV)
                .popularity(tv.getPopularity())
                .posterPath(tv.getPosterPath())
                .voteAverage(tv.getVoteAverage())
                .voteCount(tv.getVoteCount())
                .year(tv.getYear())
                .name(tv.getNameKo())
                .nameOriginal(tv.getNameOriginal())
                .build();
    }

    public static ContentSummary fromPerson(Person person) {
        return ContentSummary.builder()
                .contentId(person.getTmdbId())
                .mediaType(MediaType.PERSON)
                .popularity(person.getPopularity())
                .name(person.getNameKo())
                .nameOriginal(person.getNameOriginal())
                .knownForDepartment(person.getKnownForDepartment())
                .profilePath(person.getProfilePath())
                .build();
    }
}
