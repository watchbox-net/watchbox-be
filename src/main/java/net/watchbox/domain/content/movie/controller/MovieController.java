package net.watchbox.domain.content.movie.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.movie.service.MovieService;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MovieController {
    private final MovieService movieService;

    // 인기 영화 리스트 조회
    // 높은 평점 영화 리스트 조회
}
