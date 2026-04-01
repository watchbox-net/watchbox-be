//package net.watchbox.global.deprecated;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import net.watchbox.global.dev.deprecated.response.MovieListResponse;
//import net.watchbox.global.dto.response.ApiResponse;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/api/contents/movie")
//@Tag(name = "Movie API")
//public class MovieController {
//    private final MovieService movieService;
//
//    // 인기 영화 리스트 조회
//    @GetMapping("/popular")
//    public ResponseEntity<ApiResponse<MovieListResponse>> getPopularMovies(
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "KR") String region) {
//        return ResponseEntity.ok(
//                ApiResponse.success(movieService.getPopularMovies(page, region))
//        );
//    }
//
//    // 높은 평점 영화 리스트 조회
//}
