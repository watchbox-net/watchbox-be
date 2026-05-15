package net.watchbox.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 앱 시작 시 box 테이블의 AUTO_INCREMENT 시작값을 120 으로 설정.
 * 현재 AUTO_INCREMENT 가 이미 120 이상이면 그대로 둔다.
 * prod, dev 환경에서만 동작.
 */
@Slf4j
@Component
@Profile({"prod", "dev"})
@RequiredArgsConstructor
public class AutoIncrementInitializer implements ApplicationRunner {

    private static final long START_VALUE = 120L;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long currentAutoIncrement = jdbcTemplate.queryForObject(
                "SELECT AUTO_INCREMENT FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = 'box'",
                Long.class
        );

        if (currentAutoIncrement == null) {
            log.warn("[BoxAutoIncrementInitializer] box 테이블 AUTO_INCREMENT 조회 실패");
            return;
        }

        if (currentAutoIncrement >= START_VALUE) {
            log.info("[BoxAutoIncrementInitializer] box AUTO_INCREMENT={} 이미 {} 이상이라 변경 안 함",
                    currentAutoIncrement, START_VALUE);
            return;
        }

        jdbcTemplate.execute("ALTER TABLE box AUTO_INCREMENT = " + START_VALUE);
        log.info("[BoxAutoIncrementInitializer] box AUTO_INCREMENT {} -> {} 설정 완료",
                currentAutoIncrement, START_VALUE);
    }
}
