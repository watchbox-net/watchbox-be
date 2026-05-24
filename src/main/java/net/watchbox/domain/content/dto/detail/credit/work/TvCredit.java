package net.watchbox.domain.content.dto.detail.credit.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.watchbox.global.tmdb.configuration.Department;
import net.watchbox.domain.record.dto.type.WatchMediaType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class TvCredit implements CombinedCredit {
    private Long tmdbId;
    private WatchMediaType watchMediaType;     // 항상 TV (프론트 분기용 discriminator)

    private String posterPath;
    private String name;

    private List<CreditRole> creditRoleList;   // [CAST] / [CREW] / [CAST, CREW]
    private String character;                  // CAST 일 때만
    private List<Department> departmentList;   // CREW 일 때 부서들 (감독+제작+각본 등)

    private Integer year;
    private LocalDate firstAirDate;

    private Double popularity;

    @Override
    public LocalDate getSortDate() {
        return firstAirDate;
    }
}
