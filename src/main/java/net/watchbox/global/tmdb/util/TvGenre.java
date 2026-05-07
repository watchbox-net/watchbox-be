package net.watchbox.global.tmdb.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum TvGenre {
    ACTION_ADVENTURE(10759, "Action & Adventure", "액션·어드벤처"),
    ANIMATION(16, "Animation", "애니메이션"),
    COMEDY(35, "Comedy", "코미디"),
    CRIME(80, "Crime", "범죄"),
    DOCUMENTARY(99, "Documentary", "다큐멘터리"),
    DRAMA(18, "Drama", "드라마"),
    FAMILY(10751, "Family", "가족"),
    KIDS(10762, "Kids", "키즈"),
    MYSTERY(9648, "Mystery", "미스터리"),
    NEWS(10763, "News", "뉴스"),
    REALITY(10764, "Reality", "리얼리티"),
    SCI_FI_FANTASY(10765, "Sci-Fi & Fantasy", "SF·판타지"),
    SOAP(10766, "Soap", "소프 오페라"),
    TALK(10767, "Talk", "토크쇼"),
    WAR_POLITICS(10768, "War & Politics", "전쟁·정치"),
    WESTERN(37, "Western", "서부");

    private final int id;
    private final String englishName;
    private final String name;

    private static final Map<Integer, TvGenre> ID_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(TvGenre::getId, g -> g));

    // ID로 Enum 찾기
    public static TvGenre findById(int id) {
        return ID_MAP.get(id);
    }

    // ID로 한글 장르명 가져오기
    public static String getNameById(int id) {
        TvGenre genre = findById(id);
        return genre != null ? genre.getName() : null;
    }

    // ID로 영문 장르명 가져오기
    public static String getEnglishNameById(int id) {
        TvGenre genre = findById(id);
        return genre != null ? genre.getEnglishName() : null;
    }

    /**
     * 장르 ID 리스트를 한글명으로 매핑
     * 최대 3개까지만 반환 (목록 페이지용)
     * "액션·어드벤처" 같이 합성된 장르는 가운뎃점으로 분리해서 평탄화 후 3개로 제한
     */
    public static List<String> mapSummaryGenreIdListToKorean(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }

        return genreIds.stream()
                .limit(3)  // 먼저 3개 장르 ID만 선택
                .map(TvGenre::getNameById)
                .filter(Objects::nonNull)
                .flatMap(name -> Arrays.stream(name.split("·")))  // 그 다음에 split
                .limit(3)  // 콤마로 나눈 후의 결과에서 3개만
                .toList();
    }

    /**
     * 장르 ID 리스트를 한글명으로 매핑
     * 제한 없이 모두 반환 (상세 페이지용)
     * "액션·어드벤처" 같은 합성 장르도 분리하지 않고 원형 유지
     */
    public static List<String> mapDetailGenreIdListToKorean(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }

        return genreIds.stream()
                .limit(3)  // 먼저 3개 장르 ID만 선택
                .map(TvGenre::getNameById)
                .filter(Objects::nonNull)
                .flatMap(name -> Arrays.stream(name.split("·")))  // 그 다음에 split
                .toList();

        // 원문
//        return genreIds.stream()
//                .map(TvGenre::getNameById)
//                .filter(Objects::nonNull)
//                .toList();
    }
}
