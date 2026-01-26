package net.watchbox.domain.content.tv.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.tv.service.TvService;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TvController {
    private final TvService tvService;

    // 인기 TV 리스트 조회
    // 높은 평점 TV 리스트 조회
}
