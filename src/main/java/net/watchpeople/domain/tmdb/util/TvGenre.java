package net.watchpeople.domain.tmdb.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TvGenre {
    ACTION_ADVENTURE(10759, "Action & Adventure"),
    ANIMATION(16, "Animation"),
    COMEDY(35, "Komödie"),
    CRIME(80, "Krimi"),
    DOCUMENTARY(99, "Dokumentarfilm"),
    DRAMA(18, "Drama"),
    FAMILY(10751, "Familie"),
    KIDS(10762, "Kids"),
    MYSTERY(9648, "Mystery"),
    NEWS(10763, "News"),
    REALITY(10764, "Reality"),
    SCI_FI_FANTASY(10765, "Sci-Fi & Fantasy"),
    SOAP(10766, "Soap"),
    TALK(10767, "Talk"),
    WAR_POLITICS(10768, "War & Politics"),
    WESTERN(37, "Western");

    private final int id;
    private final String name;

    // ID로 장르명 가져오기
    public static String getNameById(int id) {
        TvGenre genre = findById(id);
        return genre != null ? genre.getName() : null;
    }

    // ID로 Enum 찾기
    public static TvGenre findById(int id) {
        for (TvGenre genre : TvGenre.values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        return null;
    }

    // 장르명으로 Enum 찾기
    public static TvGenre findByName(String name) {
        for (TvGenre genre : TvGenre.values()) {
            if (genre.name.equals(name)) {
                return genre;
            }
        }
        return null;
    }
}
