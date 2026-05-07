package net.watchbox.global.tmdb.inner.credit.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.domain.content.sub.person.entity.Department;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbAggregateCrewItem {

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

    /** 시리즈 내에서 한 스태프가 맡은 직무 목록 (한 부서에서 여러 job 가능) */
    private List<TmdbAggregateCrewJobItem> jobs;

    private Department department;

    @JsonProperty("total_episode_count")
    private Integer totalEpisodeCount;
}
