package net.watchpeople.domain.content.person.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.common.entity.Content;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "person")
@DiscriminatorValue("PERSON")
public class Person extends Content {
    // person 테이블의 PK도 tmdb_id

    private String nameKo;
    private String nameEn;
    private String nameOriginal;
    private String profilePath;
    private String knownForDepartment;
    private Double popularity;

    @ElementCollection
    @CollectionTable(name = "known_for", joinColumns = @JoinColumn(name = "tmdb_id"))
    private List<KnownFor> knownFor;

}
