package net.watchbox.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("tmdb")
public class TmdbProperties {
    private String imageUrl;
    private Api api;

    @Setter
    @Getter
    public static class Api {
        private String key;
        private String baseUrl;
        private boolean includeAdult;
    }
}