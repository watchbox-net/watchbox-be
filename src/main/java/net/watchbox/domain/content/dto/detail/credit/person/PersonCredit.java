package net.watchbox.domain.content.dto.detail.credit.person;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class PersonCredit {
    private List<Cast> castList;
    private List<Crew> crewList;
    private Long totalCount;
    private Long castCount;
    private Long crewCount;
}
