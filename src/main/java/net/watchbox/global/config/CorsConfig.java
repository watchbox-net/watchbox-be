package net.watchbox.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {
    @Value("${url.frontend}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                // 운영/개발 프론트 + 로컬 WebView 앱 테스트(맥 LAN IP) 허용.
                // allowCredentials(true) 와 함께 쓰려면 allowedOriginPatterns 사용.
                .allowedOriginPatterns(
                        frontendUrl,
                        "http://localhost:4000",
                        "http://192.168.*.*:4000"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
