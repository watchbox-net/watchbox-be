package net.watchbox.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .addServersItem(new Server().url("/"));
//                .tags(List.of(
//                        new Tag().name("DevAccount"),
//                        new Tag().name("Member"),
//                        new Tag().name("MyBox"),
//                        new Tag().name("MyBoxContent"),
//                        new Tag().name("SharedBox"),
//                        new Tag().name("SharedBoxContent"),
//                        new Tag().name("SharedBoxInvitation"),
//                        new Tag().name("SearchContent"),
//                        new Tag().name("TV API"),
//                        new Tag().name("Movie API"),
//                        new Tag().name("ContentRecord"),
//                        new Tag().name("Auth"),
//                        new Tag().name("HealthCheck"),
//                        new Tag().name("DevWatchRecord")
//                ));
    }

    /**
     * Swagger description 에 API 개수 노출 customizer.
     * - Dev 컨트롤러는 제외 ("/dev/" path prefix 또는 "Dev" 로 시작하는 tag 기준)
     * - 운영 API 와 Dev API 를 분리해서 카운트
     */
    @Bean
    public OpenApiCustomizer apiCountCustomizer() {
        return openApi -> {
            long productionApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith("/dev/"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    .filter(op -> op.getTags() == null
                            || op.getTags().stream().noneMatch(t -> t.startsWith("Dev")))
                    .count();

            long devApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("/dev/"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    .count();

            String original = openApi.getInfo().getDescription();
            openApi.getInfo().setDescription(
                    (original == null ? "" : original)
                            + String.format("%n%n**운영 API: %d개**  /  Dev(hidden 제외): %d개", productionApis, devApis)
            );
        };
    }
}
