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
public enum MovieGenre {
    ACTION(28, "Action", "액션"),
    ADVENTURE(12, "Adventure", "모험"),
    ANIMATION(16, "Animation", "애니메이션"),
    COMEDY(35, "Comedy", "코미디"),
    CRIME(80, "Crime", "범죄"),
    DOCUMENTARY(99, "Documentary", "다큐멘터리"),
    DRAMA(18, "Drama", "드라마"),
    FAMILY(10751, "Family", "가족"),
    FANTASY(14, "Fantasy", "판타지"),
    HISTORY(36, "History", "역사"),
    HORROR(27, "Horror", "공포"),
    MUSIC(10402, "Music", "음악"),
    MYSTERY(9648, "Mystery", "미스터리"),
    ROMANCE(10749, "Romance", "로맨스"),
    SCIENCE_FICTION(878, "Science Fiction", "SF"),
    TV_MOVIE(10770, "TV Movie", "TV 영화"),
    THRILLER(53, "Thriller", "스릴러"),
    WAR(10752, "War", "전쟁"),
    WESTERN(37, "Western", "서부");

    private final int id;
    private final String englishName;
    private final String koreanName;

    private static final Map<Integer, MovieGenre> ID_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(MovieGenre::getId, g -> g));

    // ID로 Enum 찾기
    public static MovieGenre findById(int id) {
        return ID_MAP.get(id);
    }

    // ID로 한글 장르명 가져오기
    public static String getKoreanNameById(int id) {
        MovieGenre genre = findById(id);
        return genre != null ? genre.getKoreanName() : null;
    }

    // ID로 영문 장르명 가져오기
    public static String getEnglishNameById(int id) {
        MovieGenre genre = findById(id);
        return genre != null ? genre.getEnglishName() : null;
    }

    /**
     * 장르 ID 리스트를 한글명으로 매핑
     * 최대 3개까지만 반환
     */
    public static List<String> mapGenreIdListToKorean(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }

        return genreIds.stream()
                .limit(3)
                .map(MovieGenre::getKoreanNameById)
                .filter(Objects::nonNull)
                .toList();
    }
}
