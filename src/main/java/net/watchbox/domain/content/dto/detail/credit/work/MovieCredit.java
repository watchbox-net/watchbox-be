package net.watchbox.domain.content.dto.detail.credit.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.watchbox.global.tmdb.configuration.Department;
import net.watchbox.domain.record.dto.request.WatchMediaType;

import java.time.LocalDate;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class MovieCredit implements CombinedCredit {
    private Long tmdbId;
    private WatchMediaType watchMediaType;     // 항상 MOVIE (프론트 분기용 discriminator)

    private String posterPath;
    private String title;

    private CreditRole creditRole;             // CAST | CREW
    private String character;                  // CAST 일 때만
    private Department department;             // CREW 일 때만

    private Integer year;
    private LocalDate releaseDate;

    private Double popularity;

    @Override
    public LocalDate getSortDate() {
        return releaseDate;
    }
}
