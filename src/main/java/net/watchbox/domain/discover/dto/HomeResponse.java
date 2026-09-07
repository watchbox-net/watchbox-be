package net.watchbox.domain.discover.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import net.watchbox.domain.content.dto.list.ContentItem;

import java.util.List;

/**
 * 홈 화면 8개 섹션을 한 번에 담는 응답.
 *
 * <p>개별 discover 엔드포인트와 달리 페이지네이션 메타(totalPages 등)는 싣지 않는다.
 * 홈은 각 섹션 1페이지만 가로 캐러셀로 보여주고 더 넘기지 않기 때문.
 * 섹션 더보기는 기존 {@code /discover/{category}/{type}} 엔드포인트가 담당한다.
 *
 * <p>일부 섹션이 실패해도 나머지는 내려간다(실패 섹션은 빈 리스트).
 */
@Schema(description = "홈 화면 섹션 묶음 응답")
public record HomeResponse(
        @Schema(description = "트렌딩 영화") List<ContentItem> trendingMovies,
        @Schema(description = "트렌딩 시리즈") List<ContentItem> trendingTv,
        @Schema(description = "인기 영화") List<ContentItem> popularMovies,
        @Schema(description = "인기 시리즈") List<ContentItem> popularTv,
        @Schema(description = "상영중 영화") List<ContentItem> nowShowingMovies,
        @Schema(description = "방영중 시리즈") List<ContentItem> nowShowingTv,
        @Schema(description = "평점높은 영화") List<ContentItem> topRatedMovies,
        @Schema(description = "평점높은 시리즈") List<ContentItem> topRatedTv
) {
}
