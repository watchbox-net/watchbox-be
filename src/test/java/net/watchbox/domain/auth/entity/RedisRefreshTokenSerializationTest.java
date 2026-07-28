package net.watchbox.domain.auth.entity;

import net.watchbox.domain.auth.repository.RedisRefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RedisRefreshToken 의 OffsetDateTime(KST) 필드가 실제 Redis 에 저장/조회 라운드트립 되는지 검증.
 * 로컬 Redis(6379) 필요. Spring Data Redis 컨버터가 OffsetDateTime 을 지원하지 않으면 save() 에서 예외.
 *
 * <p>메인 앱 클래스({@code @EnableJpaAuditing})를 로드하면 Redis 슬라이스에서 jpaMappingContext 가
 * 없어 컨텍스트 로드가 실패하므로, 최소 {@link TestApp} 설정만 명시해 JPA 관련 빈을 배제한다.
 */
@DataRedisTest
@ContextConfiguration(classes = RedisRefreshTokenSerializationTest.TestApp.class)
class RedisRefreshTokenSerializationTest {

    private static final Long TEST_MEMBER_ID = 999_999_999L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @SpringBootConfiguration
    @EnableRedisRepositories(basePackageClasses = RedisRefreshTokenRepository.class)
    static class TestApp {
    }

    @Autowired
    private RedisRefreshTokenRepository repository;

    @AfterEach
    void cleanup() {
        repository.deleteById(TEST_MEMBER_ID);
    }

    @Test
    void offsetDateTime_필드가_라운드트립된다() {
        OffsetDateTime now = OffsetDateTime.now(KST);

        repository.save(RedisRefreshToken.builder()
                .memberId(TEST_MEMBER_ID)
                .nickname("테스트")
                .token("dummy-token")
                .ip("127.0.0.1")
                .deviceInfo("junit")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .lastUsedAt(now)
                .ttl(60L)
                .build());

        RedisRefreshToken found = repository.findById(TEST_MEMBER_ID).orElseThrow();

        assertThat(found.getIssuedAt().toInstant()).isEqualTo(now.toInstant());
        assertThat(found.getLastUsedAt().toInstant()).isEqualTo(now.toInstant());
        assertThat(found.getExpiresAt().toInstant()).isEqualTo(now.plusSeconds(60).toInstant());
    }
}
