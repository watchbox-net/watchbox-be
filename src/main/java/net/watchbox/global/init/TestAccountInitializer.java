package net.watchbox.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class TestAccountInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        insertOauthAccountIfAbsent(-1L, "id001", "tester1@gmail.com", "tester1");
        insertOauthAccountIfAbsent(-2L, "id002", "tester2@gmail.com", "tester2");
        insertOauthAccountIfAbsent(-3L, "id003", "tester3@gmail.com", "tester3");
        insertOauthAccountIfAbsent(-4L, "id004", "tester4@gmail.com", "tester4");
        insertOauthAccountIfAbsent(-5L, "id005", "tester5@gmail.com", "tester5");

        insertMemberIfAbsent(-1L, -1L, "tester1@gmail.com", "tester1");
        insertMemberIfAbsent(-2L, -2L, "tester2@gmail.com", "tester2");
        insertMemberIfAbsent(-3L, -3L, "tester3@gmail.com", "tester3");
        insertMemberIfAbsent(-4L, -4L, "tester4@gmail.com", "tester4");
        insertMemberIfAbsent(-5L, -5L, "tester5@gmail.com", "tester5");

        log.info("[TestAccountInitializer] 테스트 계정 초기화 완료");
    }

    private void insertOauthAccountIfAbsent(Long accountId, String oauthId, String email, String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM oauth_account WHERE account_id = ?",
                Integer.class, accountId
        );
        if (count != null && count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO oauth_account (account_id, oauth_provider, oauth_id, email, name) VALUES (?, 'GOOGLE', ?, ?, ?)",
                    accountId, oauthId, email, name
            );
        }
    }

    private void insertMemberIfAbsent(Long memberId, Long oauthAccountId, String email, String nickname) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM member WHERE member_id = ?",
                Integer.class, memberId
        );
        if (count != null && count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO member (member_id, oauth_account_id, email, nickname) VALUES (?, ?, ?, ?)",
                    memberId, oauthAccountId, email, nickname
            );
        }
    }
}
