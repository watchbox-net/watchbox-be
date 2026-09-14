package net.watchbox.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .scheme("bearer")
                .bearerFormat("JWT");
        SecurityScheme adminKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Admin-Key");
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token")
                .addList("Admin Key");

        Info info = new Info()
                .title("WatchBox" + " API 명세서")
                .version("v1.0")
                .description("WatchBox API 명세서입니다.");

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes("Bearer Token", apiKey)
                        .addSecuritySchemes("Admin Key", adminKey))
                .addSecurityItem(securityRequirement)
                .addServersItem(new Server().url("/"))
                .tags(tags());
    }

    /**
     * 태그 이름·설명을 한곳에 모은다. 이 목록이 곧 <b>API 명세서의 목차</b>다.
     *
     * <p>이름 규칙은 {@code {그룹}-{순번} [도메인] 이름}. 앞의 번호가 정렬 기준이라
     * 스웨거 화면이 <b>도메인 흐름대로</b> 나온다(알파벳순이면 Admin 이 맨 위로 온다).
     *
     * <p>설명을 컨트롤러가 아니라 여기 두는 이유는 <b>전체를 한눈에 보기 위해서</b>다.
     * 컨트롤러에는 이름만 남긴다 — 20여 개 파일을 열어봐야 목록을 파악할 수 있으면 관리가 안 된다.
     *
     * <p>번호대: 0 인증 / 1 회원 / 2 콘텐츠 / 3 시청기록 / 4 박스 / 8 운영 / 9 개발·헬스체크
     *
     * <p>9 번대는 문서에는 공개하되 호출은 {@code X-Admin-Key} 로 막는다(AdminAuthFilter).
     */
    private List<Tag> tags() {
        return List.of(
                tag("0-1 [Auth] Social Login", "소셜 로그인 (구글·애플 / 앱·웹)"),
                tag("0-2 [Auth] Token", "토큰 재발급 · 로그아웃"),

                tag("1-1 [Member] Member", "회원 관리"),
                tag("1-2 [Member] Notification", "알림 - 실시간 SSE 스낵바"),
                tag("1-3 [Member] Web Push", "웹 푸시 구독 등록 / VAPID 공개키 (POC)"),
                tag("1-4 [Member] File", "파일(이미지·영상) 업로드 · 삭제"),

                tag("2-1 [Content] Discover", "홈 · 트렌드 목록 (영화 · 시리즈)"),
                tag("2-2 [Content] Search", "콘텐츠 검색"),
                tag("2-3 [Content] Detail", "콘텐츠 상세 조회"),

                tag("3-1 [Record] Watch Record", "시청 상태 · 좋아요 · 기록 이력"),

                tag("4-1 [Box] Box", "박스 통합 - 생성 · 조회 · 수정 · 활동 이력"),
                tag("4-2 [Box] Content", "박스 콘텐츠 조회 · 추가 · 삭제"),
                tag("4-3 [Box] Content Sheet", "콘텐츠 기준 박스 포함 여부 조회 및 일괄 추가/삭제"),
                tag("4-4 [Box] Invitation", "공유 박스 초대 - 발송 · 수락 · 거절 · 취소"),

                tag("8-1 [Admin] Admin", "관리자"),
                tag("8-2 [Admin] Preview", "샘플 화면"),

                tag("9-1 [Dev] Account", "테스트 계정"),
                tag("9-2 [Dev] TMDB", "TMDB 원본 응답 확인"),
                tag("9-3 [Dev] Infra", "전송 경로 전환 · Redis · 알림 실패 주입"),
                tag("9-5 [Health] Spring", "서버 상태 · 정보 · 시간 확인"),
                tag("9-6 [Health] Infra", "인프라(DB · Redis · Kafka) 연결 확인")
        );
    }

    private Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }

    /**
     * 오퍼레이션이 하나도 없는 태그 선언을 목차에서 지운다.
     *
     * <p>{@link #tags()} 는 전체 목차를 한곳에 모아두는 곳이라 환경을 가리지 않는다. 반면 컨트롤러는
     * {@code @Profile} 이나 {@code @Hidden} 으로 빠질 수 있어, <b>선언만 남고 내용이 비는</b> 태그가
     * 생긴다 — 운영 스웨거에 {@code 9-1 [Dev] Account} 헤더만 덩그러니 뜨는 식이다.
     *
     * <p>프로파일을 조건으로 걸지 않는 이유는 <b>빠지는 방식이 여러 가지</b>이기 때문이다.
     * 실제 오퍼레이션을 기준으로 하면 어느 경로로 빠지든 알아서 맞는다.
     */
    @Bean
    public OpenApiCustomizer unusedTagCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) {
                return;
            }
            Set<String> used = openApi.getPaths().values().stream()
                    .flatMap(path -> path.readOperations().stream())
                    .filter(op -> op.getTags() != null)
                    .flatMap(op -> op.getTags().stream())
                    .collect(Collectors.toSet());

            openApi.setTags(openApi.getTags().stream()
                    .filter(tag -> used.contains(tag.getName()))
                    .toList());
        };
    }

    /**
     * Swagger description 에 API 개수 노출 customizer.
     * - 운영 API: "/dev/", "/health" path prefix 와 [Dev]/[Health] 태그 제외
     * - Dev API: "/dev/" path prefix
     * - HealthCheck API: "/health" path prefix
     */
    @Bean
    public OpenApiCustomizer apiCountCustomizer() {
        return openApi -> {
            long productionApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith("/dev/"))
                    .filter(entry -> !entry.getKey().startsWith("/health"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    // 태그 이름이 "9-3 [Dev] Infra" 형태라 접두어가 아니라 대괄호 그룹으로 가른다.
                    .filter(op -> op.getTags() == null
                            || op.getTags().stream().noneMatch(t -> t.contains("[Dev]")))
                    .filter(op -> op.getTags() == null
                            || op.getTags().stream().noneMatch(t -> t.contains("[Health]")))
                    .count();

            long devApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("/dev/"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    .count();

            long healthApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("/health"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    .count();

            String summary = String.format(
                    "%n%n**운영 API: %d개**  /  Dev(hidden 제외): %d개  /  HealthCheck: %d개",
                    productionApis, devApis, healthApis);

            String original = openApi.getInfo().getDescription();
            openApi.getInfo().setDescription((original == null ? "" : original) + summary);
        };
    }
}
