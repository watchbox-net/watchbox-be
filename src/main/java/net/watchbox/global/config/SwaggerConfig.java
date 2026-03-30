package net.watchbox.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
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
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token");

        Info info = new Info()
                .title("WatchBox" + " API 명세서")
                .version("v1.0")
                .description("WatchBox API 명세서입니다.");

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
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
}
