package net.watchbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WatchBoxApplication {
    public static void main(String[] args) {
        SpringApplication.run(WatchBoxApplication.class, args);
    }
}
