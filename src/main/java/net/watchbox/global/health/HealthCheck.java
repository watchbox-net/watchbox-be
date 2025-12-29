package net.watchbox.global.health;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/health")
@Slf4j
@Tag(name = "HealthCheck")
public class HealthCheck {
    @GetMapping
    public String healthCheck() {
        return "WatchBox Server Success Health Check!";
    }

    @GetMapping("/info")
    public String info(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        String response = String.format(
                "서버 정보 확인 %s - IP: %s - User-Agent: %s - 요청URL: %s",
                ZonedDateTime.now(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                requestUrl
        );
        return response;
    }

    @GetMapping("/time")
    public ZonedDateTime time() {
        return ZonedDateTime.now();
    }

//    @GetMapping("/health/db/mysql")
}
