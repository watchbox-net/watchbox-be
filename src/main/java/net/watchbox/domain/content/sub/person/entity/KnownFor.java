package net.watchbox.domain.content.sub.person.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import net.watchbox.domain.content.entity.MediaType;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Embeddable
public class KnownFor {
    private Long knownForId; // TMDB ID

    private Long title;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
}