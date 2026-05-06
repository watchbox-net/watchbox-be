package net.watchbox.domain.content.sub.person.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.global.entity.BaseTime;

import java.util.List;

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
    private String nameOriginal; // 이건 사실상 정확히 가져오기는 불가능, 영문이름으로만 표기해야할듯
    private String profilePath;
    private Department knownForDepartment;
    private Double popularity;

    @ElementCollection
    @CollectionTable(name = "known_for", joinColumns = @JoinColumn(name = "content_id"))
    private List<KnownFor> knownFor; // Search 결과의 작품 리스트

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonDetail personDetail;

}
