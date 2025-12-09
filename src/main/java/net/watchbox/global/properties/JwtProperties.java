package net.watchbox.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String issuer;
    private String secretKey;

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration refreshTokenExpiry;

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration accessTokenExpiry;
}
