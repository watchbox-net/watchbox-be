//package net.watchbox.global.dev.controller;
//
//import io.swagger.v3.oas.annotations.Hidden;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.watchbox.global.dev.dto.DevMultiSearchRequest;
//import net.watchbox.global.dev.dto.DevMultiSearchResponseDto;
//import net.watchbox.global.dev.service.DevSearchService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/dev/tmdb/search")
//@Tag(name = "DevTmdb")
//@Hidden
//public class DevTmdbSearchController {
//    private final DevSearchService searchService;
//
//    /**
//     * SEARCH Movie/TV/People 검색 API
//     * TMDB POST /api/movies/search
//     *
//     * Request Body:
//     * {
//     *   "query": "어벤저스",
//     *   "language": "ko-KR",
//     *   "page": 1,
//     *   "type": "movie"  // movie, tv, multi
//     * }
//     */
//    @PostMapping
//    public Mono<ResponseEntity<DevMultiSearchResponseDto>> searchAll(
//            @Valid @RequestBody DevMultiSearchRequest request) {
//
//        log.info("영화 검색 요청: query={}, type={}, page={}",
//                request.getQuery(), request.getType(), request.getPage());
//
//        return searchService.searchAll(request)
//                .map(response -> {
//                    log.info("검색 결과: {} 건", response.getTotalResults());
//                    return ResponseEntity.ok(response);
//                })
//                .defaultIfEmpty(ResponseEntity.noContent().build());
//    }
//
//    /**
//     * GET 방식 검색 (간단한 검색용)
//     * GET /api/movies/search?query=어벤저스&type=movie&language=ko-KR&page=1
//     */
//    @GetMapping
//    public Mono<ResponseEntity<DevMultiSearchResponseDto>> searchAllGet(
//            @RequestParam String query,
//            @RequestParam(required = false, defaultValue = "multi") String type,
//            @RequestParam(required = false, defaultValue = "ko-KR") String language,
//            @RequestParam(required = false, defaultValue = "1") int page) {
//
//        DevMultiSearchRequest request = new DevMultiSearchRequest(query, language, page, true, type);
//
//        log.info("영화 검색 요청(GET): query={}, type={}, page={}", query, type, page);
//
//        return searchService.searchAll(request)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.noContent().build());
//    }
//
//    /**
//     * 인기 영화 목록
//     * GET /api/movies/popular?language=ko-KR&page=1
//     */
//    @GetMapping("/popular")
//    public Mono<ResponseEntity<DevMultiSearchResponseDto>> getPopularMovies(
//            @RequestParam(required = false, defaultValue = "ko-KR") String language,
//            @RequestParam(required = false, defaultValue = "1") int page) {
//
//        log.info("인기 영화 조회: language={}, page={}", language, page);
//
//        return searchService.getPopularMovies(language, page)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.noContent().build());
//    }
//
//    /**
//     * 현재 상영중인 영화
//     * GET /api/movies/now-playing?language=ko-KR&page=1
//     */
//    @GetMapping("/now-playing")
//    public Mono<ResponseEntity<DevMultiSearchResponseDto>> getNowPlayingMovies(
//            @RequestParam(required = false, defaultValue = "ko-KR") String language,
//            @RequestParam(required = false, defaultValue = "1") int page) {
//
//        log.info("현재 상영 영화 조회: language={}, page={}", language, page);
//
//        return searchService.getNowPlayingMovies(language, page)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.noContent().build());
//    }
//
//    /**
//     * 영화 전용 검색
//     * POST /api/movies/search/movie
//     */
//    @PostMapping("/search/movie")
//    public Mono<ResponseEntity<DevMultiSearchResponseDto>> searchOnlyMovies(
//            @Valid @RequestBody DevMultiSearchRequest request) {
//
//        request.setType("movie");
//
//        log.info("영화 전용 검색: query={}, page={}", request.getQuery(), request.getPage());
//
//        return searchService.searchAll(request)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.noContent().build());
//    }
//
//    /**
//     * TV 프로그램 전용 검색
//     * POST /api/movies/search/tv
//     */
//    @PostMapping("/search/tv")
//    public Mono<ResponseEntity<DevMultiSearchResponseDto>> searchOnlyTv(
//            @Valid @RequestBody DevMultiSearchRequest request) {
//
//        request.setType("tv");
//
//        log.info("TV 프로그램 검색: query={}, page={}", request.getQuery(), request.getPage());
//
//        return searchService.searchAll(request)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.noContent().build());
//    }
//}
