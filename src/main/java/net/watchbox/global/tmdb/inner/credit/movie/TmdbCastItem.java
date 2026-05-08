package net.watchbox.global.tmdb.inner.credit.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbCastItem {
    private boolean adult;

    private Integer gender;

    private Long id;

    /**
     * 인물의 주 활동 분야 (Acting, Directing, Creator 등).
     * TMDB가 enum 외 값을 보낼 수 있어 String 으로 보관.
     */
    @JsonProperty("known_for_department")
    private String knownForDepartment;

    private String name;

    @JsonProperty("original_name")
    private String originalName;

    private Double popularity;

    @JsonProperty("profile_path")
    private String profilePath;

    @JsonProperty("cast_id")
    private Long castId;

    private String character;

    @JsonProperty("credit_id")
    private String creditId;

    private Integer order;
}
