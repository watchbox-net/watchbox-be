package net.watchpeople.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class OauthAccount extends BaseTime {
    //UserDetails를 상속 받아 인증 객체로 사용
    //사용자의 인증 정보와 권한 정보를 저장하는 메서드 제공
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", updatable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, updatable = false)
    private OauthProvider oauthProvider;

    @OneToOne(mappedBy = "oauthAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private Member member;

    @Column(name = "oauthId", nullable = false, updatable = false, unique = true)
    private String oauthId; // 소셜로그인 ID

    @Column(name = "email", nullable = false, updatable = false) //, unique = true) ToDo
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name="name")
    private String name;

}
