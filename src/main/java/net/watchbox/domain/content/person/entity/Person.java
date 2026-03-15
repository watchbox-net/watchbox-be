package net.watchbox.domain.content.person.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.base.entity.Content;
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
    private String nameOriginal; // 이건 사실상 정확히 가져오기는 불가능, 영문이름으로만 표기해야할듯
    private String profilePath;
    private String knownForDepartment;
    private Double popularity;

    @ElementCollection
    @CollectionTable(name = "known_for", joinColumns = @JoinColumn(name = "tmdb_id"))
    private List<KnownFor> knownFor; // Search 결과의 작품 리스트

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonDetail personDetail;

}
