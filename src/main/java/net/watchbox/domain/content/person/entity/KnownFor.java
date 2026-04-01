package net.watchbox.domain.content.person.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import net.watchbox.domain.content.base.entity.MediaType;

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