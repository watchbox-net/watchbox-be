package net.watchbox.global.dev.deprecated.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.sub.person.entity.Department;
import net.watchbox.domain.content.sub.person.entity.KnownFor;
import net.watchbox.domain.content.sub.person.entity.Person;

import java.util.List;

@Getter
@ToString
@Builder
public class PersonResponse {
    private Long id; // tmdbId
    private String nameKo;
    private String nameEn;
    private String nameOriginal;
    private String profilePath;
    private Department knownForDepartment;
    private Double popularity;
//    private List<KnownFor> knownFor;

    public static PersonResponse from(Person person){
        return PersonResponse.builder()
                .id(person.getTmdbId())
                .nameKo(person.getNameKo())
                .nameEn(person.getNameEn())
                .nameOriginal(person.getNameOriginal())
                .profilePath(person.getProfilePath())
                .knownForDepartment(person.getKnownForDepartment())
                .popularity(person.getPopularity())
//                .knownFor(person.getKnownFor())
                .build();
    }
}
