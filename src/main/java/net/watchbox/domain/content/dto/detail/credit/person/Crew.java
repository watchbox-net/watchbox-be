package net.watchbox.domain.content.dto.detail.credit.person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.watchbox.domain.content.sub.person.entity.Department;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Crew {
    private Long tmdbId;
    private String profilePath;                 // 프로필 이미지
    private String name;                        // 이름
    private String nameOriginal;                // 원래 이름
    private Department knownForDepartment;      // 인물의 평소 활동 분야
    private List<Department> departmentList;    // 이 작품에서의 역할 (예: 연출, 각본) -> 역할 기준
}
