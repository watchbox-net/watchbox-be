package net.watchbox.domain.content.dto.list;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.sub.person.entity.Department;

import java.util.List;

@Getter
@ToString
@Builder
public class ContentSummary { // 하나의 응답 클래스에 모든 변수 포함 (Union Type 방식)
    private Long contentId;    // surrogate key (DB 저장 시에만 존재, TMDB 조회 시 null)
    private Long tmdbId;       // TMDB ID
    private MediaType mediaType;  // MOVIE, TV, PERSON
    private Double popularity;

    // 영화, TV 공통
    private String posterPath;
    private Double voteAverage;
    private Long voteCount;
    private Integer year;
//    private String overview;
    private List<String> genreList;

    // 영화 전용
    private String title; // titleKo
    private String titleOriginal;

    // TV, 인물 공통
    private String name; // nameKo
    private String nameOriginal;

    // 인물 전용
    private Department knownForDepartment;
    private List<String> knownForList;
    private String profilePath;
}
