package net.watchpeople.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchpeople.global.properties.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TmdbMovieService {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;
}
