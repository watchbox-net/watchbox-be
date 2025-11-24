package net.watchpeople.domain.content.movie.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class MovieDetail {
    @Id
    private Long tmdbId;  // Movie의 id를 그대로 사용

    @OneToOne
    @MapsId  // Movie의 id를 PK로 사용
    @JoinColumn(name = "tmdb_id")
    private Movie movie;
}
