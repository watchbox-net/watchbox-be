package net.watchbox.domain.content.dto.detail.credit.person;

import net.watchbox.domain.content.sub.person.entity.Department;

public class Crew {
    private Long tmdbId;
    private String profilePath;         // 프로필 이미지
    private String name;                // 이름
    private String nameOriginal;        // 원래 이름
    private Department knownForDepartment;  // 주요 활동 분야 (Acting, Directing 등)
    private Department department;          // 부서
    private String job;                 // 직무
}
