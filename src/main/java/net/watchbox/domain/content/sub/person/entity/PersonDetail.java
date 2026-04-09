package net.watchbox.domain.content.sub.person.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class PersonDetail {
    @Id
    private Long contentId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "content_id")
    private Person person;

    @Column(name = "biography", columnDefinition = "text")
    private String biography;
    private Integer gender; // gender: 0 = not set, 1 = female, 2 = male, 3 = non-binary
    private String knownForDepartment;  // "Acting", "Directing" 등
    private LocalDate birthday;
    private String homepage;
    private Boolean adult;
    private LocalDate deathday;
    private String placeOfBirth;
    private String imdbId;  // "nm0496932"

    // JSON으로 저장하는 방식 (나중에 필요해지면 가공)
    @Column(name = "also_known_as", columnDefinition = "json")
    private String alsoKnownAs;  // ["이병헌", "Lee Byeong-heon", ...]

}
