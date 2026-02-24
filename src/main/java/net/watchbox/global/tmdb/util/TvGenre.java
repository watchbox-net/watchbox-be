package net.watchbox.global.tmdb.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum TvGenre {
    ACTION_ADVENTURE(10759, "Action & Adventure", "액션 & 어드벤처"),
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
    SCI_FI_FANTASY(10765, "Sci-Fi & Fantasy", "SF & 판타지"),
    SOAP(10766, "Soap", "소프 오페라"),
    TALK(10767, "Talk", "토크쇼"),
    WAR_POLITICS(10768, "War & Politics", "전쟁 & 정치"),
    WESTERN(37, "Western", "서부");

    private final int id;
    private final String englishName;
    private final String koreanName;

    private static final Map<Integer, TvGenre> ID_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(TvGenre::getId, g -> g));

    // ID로 Enum 찾기
    public static TvGenre findById(int id) {
        return ID_MAP.get(id);
    }

    // ID로 한글 장르명 가져오기
    public static String getKoreanNameById(int id) {
        TvGenre genre = findById(id);
        return genre != null ? genre.getKoreanName() : null;
    }

    // ID로 영문 장르명 가져오기
    public static String getEnglishNameById(int id) {
        TvGenre genre = findById(id);
        return genre != null ? genre.getEnglishName() : null;
    }
}
