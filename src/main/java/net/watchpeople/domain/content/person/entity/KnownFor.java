package net.watchpeople.domain.content.person.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import net.watchpeople.domain.content.MediaType;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Embeddable
public class KnownFor {
    private Long knownForId; // TMDB ID

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
}