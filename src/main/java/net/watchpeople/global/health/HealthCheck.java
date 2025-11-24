package net.watchpeople.global.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheck {
    @GetMapping
    public String healthCheck() {
        return "WatchPeople Server Success Health Check!";
    }

//    @GetMapping("/health/db/mysql")
}
