package net.watchbox.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {
    // URL_CORS_ORIGINS(쉼표 구분) 를 배열로 바인딩. application.yml 의 url.cors-origins 참고.
    @Value("${url.cors-origins}")
    private String[] corsOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                // allowCredentials(true) 와 함께 쓰려면 allowedOriginPatterns 사용.
                .allowedOriginPatterns(corsOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
