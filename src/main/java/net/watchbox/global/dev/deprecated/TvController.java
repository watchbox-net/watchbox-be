//package net.watchbox.global.deprecated;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import net.watchbox.global.dev.deprecated.response.TvListResponse;
//import net.watchbox.global.dto.response.ApiResponse;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/api/contents/tv")
//@Tag(name = "TV API")
//public class TvController {
//    private final TvService tvService;
//
//    @GetMapping("/popular")
//    public ResponseEntity<ApiResponse<TvListResponse>> getPopularTvShows(
//            @RequestParam(defaultValue = "1") Integer page) {
//        return ResponseEntity.ok(
//                ApiResponse.success(tvService.getPopularTvShows(page))
//        );
//    }
//
//    // 높은 평점 TV 리스트 조회
//}
