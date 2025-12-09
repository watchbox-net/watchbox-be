package net.watchbox.domain.tmdb.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MovieGenre {
    ACTION(28, "Action"),
    ADVENTURE(12, "Abenteuer"),
    ANIMATION(16, "Animation"),
    COMEDY(35, "Komödie"),
    CRIME(80, "Krimi"),
    DOCUMENTARY(99, "Dokumentarfilm"),
    DRAMA(18, "Drama"),
    FAMILY(10751, "Familie"),
    FANTASY(14, "Fantasy"),
    HISTORY(36, "Historie"),
    HORROR(27, "Horror"),
    MUSIC(10402, "Musik"),
    MYSTERY(9648, "Mystery"),
    ROMANCE(10749, "Liebesfilm"),
    SCIENCE_FICTION(878, "Science Fiction"),
    TV_MOVIE(10770, "TV-Film"),
    THRILLER(53, "Thriller"),
    WAR(10752, "Kriegsfilm"),
    WESTERN(37, "Western");

    private final int id;
    private final String name;

    // ID로 장르명 가져오기
    public static String getNameById(int id) {
        MovieGenre genre = findById(id);
        return genre != null ? genre.getName() : null;
    }

    // ID로 Enum 찾기
    public static MovieGenre findById(int id) {
        for (MovieGenre movieGenre : MovieGenre.values()) {
            if (movieGenre.id == id) {
                return movieGenre;
            }
        }
        return null;
    }

    // 장르명으로 Enum 찾기
    public static MovieGenre findByName(String name) {
        for (MovieGenre movieGenre : MovieGenre.values()) {
            if (movieGenre.name.equals(name)) {
                return movieGenre;
            }
        }
        return null;
    }
}
