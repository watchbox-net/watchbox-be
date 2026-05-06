package net.watchbox.domain.content.dto.detail.credit.person;

import net.watchbox.domain.content.sub.person.entity.Department;

public class Cast {
    private Long tmdbId;
    private String profilePath;         // 프로필 이미지
    private String name;                // 이름
    private String nameOriginal;        // 원래 이름
    private Department knownForDepartment;  // 주요 활동 분야 (Acting, Directing 등)
    private String character;           // 배역
    private Long order;                 // 출연 순서 (order가 낮을수록 주요 출연자)
}
