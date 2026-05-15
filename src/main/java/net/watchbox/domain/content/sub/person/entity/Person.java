package net.watchbox.domain.content.sub.person.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.global.entity.BaseTime;
import net.watchbox.global.tmdb.configuration.Department;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "person")
public class Person extends BaseTime {
    @Id
    private Long contentId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(nullable = false)
    private Long tmdbId;

    private String nameKo;
    private String nameEn;
    private String nameOriginal;
    private String profilePath;
    private Department knownForDepartment;
    private Double popularity;
    private LocalDate birthday;
    private String placeOfBirth;

//    @ElementCollection
//    @CollectionTable(name = "known_for", joinColumns = @JoinColumn(name = "content_id"))
//    private List<KnownFor> knownFor; // Search 결과의 작품 리스트

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonDetail personDetail;

}
