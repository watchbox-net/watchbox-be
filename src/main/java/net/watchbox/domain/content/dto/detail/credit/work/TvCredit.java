package net.watchbox.domain.content.dto.detail.credit.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.watchbox.domain.content.sub.person.entity.Department;
import net.watchbox.domain.record.dto.request.WatchMediaType;

import java.time.LocalDate;

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

    private CreditRole creditRole;             // CAST | CREW
    private String character;                  // CAST 일 때만
    private Department department;             // CREW 일 때만

    private Integer year;
    private LocalDate firstAirDate;

    private Double popularity;

    @Override
    public LocalDate getSortDate() {
        return firstAirDate;
    }
}
