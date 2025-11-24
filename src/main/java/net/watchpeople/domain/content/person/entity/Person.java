package net.watchpeople.domain.content.person.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.TmdbContent;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "person")
@DiscriminatorValue("PERSON")
public class Person extends TmdbContent {
    /*person 테이블의 PK도 tmdb_id*/
    private String name;
    private String originalName;

    @ElementCollection
    @CollectionTable(name = "known_for", joinColumns = @JoinColumn(name = "tmdb_id"))
    private List<KnownFor> knownFor;

    private String profilePath;

    // gender: 0 = not set, 1 = female, 2 = male, 3 = non-binary
    private Integer gender;

    private String knownForDepartment;
    private Double popularity;
    private Boolean adult;
}
