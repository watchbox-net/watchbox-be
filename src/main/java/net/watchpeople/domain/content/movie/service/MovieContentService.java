package net.watchpeople.domain.content.movie.service;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.content.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieContentService {
    private final MovieRepository movieRepository;
}
