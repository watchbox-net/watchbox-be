package net.watchpeople.domain.content.person.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.common.Content;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "person")
@DiscriminatorValue("PERSON")
public class Person extends Content {
    /*person 테이블의 PK도 tmdb_id*/

    private String nameKo;
    private String nameEn;
    private String nameOriginal;

    @ElementCollection
    @CollectionTable(name = "known_for", joinColumns = @JoinColumn(name = "tmdb_id"))
    private List<KnownFor> knownFor;

    private String profilePath;
    private Integer gender; // gender: 0 = not set, 1 = female, 2 = male, 3 = non-binary
    private String knownForDepartment;
    private Double popularity;

}
