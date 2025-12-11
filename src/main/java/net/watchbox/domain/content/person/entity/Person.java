package net.watchbox.domain.content.person.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.global.entity.BaseTime;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "person")
//@DiscriminatorValue("PERSON")
public class Person extends BaseTime {
    @Id
    private Long tmdbId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false, unique = true)
    private Content content;

    private String nameKo;
    private String nameEn;
    private String nameOriginal;
    private String profilePath;
    private String knownForDepartment;
    private Double popularity;

    @ElementCollection
    @CollectionTable(name = "known_for", joinColumns = @JoinColumn(name = "tmdb_id"))
    private List<KnownFor> knownFor;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonDetail personDetail;

}
