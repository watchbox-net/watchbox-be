package net.watchbox.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "oauth_account") // 테이블명과 컬럼명은 oauth_XX 로 사용 (oAuth 미사용)
public class OAuthAccount extends BaseTime {
    //사용자의 인증 정보와 권한 정보를 저장하는 메서드 제공
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", updatable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, updatable = false)
    private OAuthProvider oauthProvider;

    /**
     * 연결된 멤버 (FK 소유측). nullable — OAuth 로그인 시 OAuthAccount 가 Member 보다 먼저 생성됨.
     * 한 Member 에 여러 소셜계정(구글·애플)을 붙일 수 있도록 N:1. (현재 로그인 로직은 provider 별
     * 별도 Member 를 만들지만, 추후 계정 연동에 대비해 관계만 N:1 로 열어둠)
     * 네이티브 로그인은 세션 종료(detach) 후 getMember() 로 참조하므로 EAGER 로 미리 로딩한다.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "oauth_id", nullable = false, updatable = false, unique = true)
    private String oauthId; // 소셜로그인 ID

    @Column(name = "email", nullable = false, updatable = false) //, unique = true) ToDo
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name="name")
    private String name;

    public void linkMember(Member member) {
        this.member = member;
    }
}
