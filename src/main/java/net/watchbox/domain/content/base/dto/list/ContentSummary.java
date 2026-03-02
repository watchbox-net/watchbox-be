package net.watchbox.domain.content.base.dto.list;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.base.entity.MediaType;

import java.util.List;

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
    private List<String> genreList; // ToDo: 장르 리스트 변환

    // 영화 전용
    private String title; // titleKo
    private String titleOriginal;

    // TV, 인물 공통
    private String name; // nameKo
    private String nameOriginal;

    // 인물 전용
    private String knownForDepartment;
    private String profilePath;
}
