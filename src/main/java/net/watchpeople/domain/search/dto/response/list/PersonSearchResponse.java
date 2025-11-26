package net.watchpeople.domain.search.dto.response.list;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
public class PersonSearchResponse extends MultiSearchResponse {
    private String name;
    private String originalName;
//    private List<KnownFor> knownFor;
    private String profilePath;
    private Integer gender;
    private String knownForDepartment;
}
