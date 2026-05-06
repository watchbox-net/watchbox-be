package net.watchbox.domain.content.dto.detail.credit.work;

import net.watchbox.domain.record.dto.request.WatchMediaType;

import java.time.LocalDate;

public final class TvCredit implements CombinedCredit {
    private Long tmdbId;
    private WatchMediaType watchMediaType;

    private String posterPath;
    private String name;

    private CreditRole creditRole;
    private String character;
    private String department;

    private Integer year;
    private LocalDate firstAirDate;
    private LocalDate lastAirDate;

    private Double popularity;
}
