package net.watchbox.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.properties.WebPushProperties;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * Web Push 인프라 설정.
 *
 * <p>BouncyCastle JCE Provider 를 등록해야 web-push 라이브러리가 P-256 ECDH 키 처리 가능.
 *
 * <p>{@link PushService} 빈은 VAPID 키쌍 + subject 로 한 번 초기화 후 모든 Push 발송에 재사용.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WebPushProperties.class)
@RequiredArgsConstructor
public class WebPushConfig {

    private final WebPushProperties properties;

    @PostConstruct
    public void registerBouncyCastle() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.info("BouncyCastle JCE Provider 등록 완료 (Web Push)");
        }
    }

    @Bean
    public PushService pushService() throws GeneralSecurityException {
        if (properties.publicKey() == null || properties.publicKey().isBlank()) {
            log.warn("WEB_PUSH_PUBLIC_KEY 미설정 — PushService 가 실제 호출되면 실패함. " +
                    "WebPushVapidKeyGenerator 로 키 발급 후 환경변수 설정 필요.");
        }
        return new PushService(properties.publicKey(), properties.privateKey(), properties.subject());
    }
}
