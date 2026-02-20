package net.watchbox.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.auth.entity.OauthAccount;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.request.BoxInvitation;
import net.watchbox.domain.box.entity.request.BoxJoinRequest;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Member implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_account_id", nullable = false)
    private OauthAccount oauthAccount;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoxInvitation> boxInvitations;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoxJoinRequest> boxJoinRequests;

    private String email;
    private String nickname;
    private String profileImage;
    private String password;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxMember> boxMembers;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxInvitation> senders;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxInvitation> receivers;

    /* ================= implements from UserDetails ================= */
    @Override // 권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override // 사용자 고유값 반환(이메일)
    public String getUsername() {
        return email;
    }

    @Override // 사용자 패스워드 반환
    public String getPassword() {
        return password;
    }

    @Override // 계정 만료 여부 반환
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않음
    }

    @Override // 계정 잠금 여부 반환
    public boolean isAccountNonLocked() {
        // 계정 잠금되었는지 확인하는 로직
        return true; // true -> 잠금되지 않음
    }

    @Override //패스워드의 만료 여부 반환
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않음
    }

    @Override // 계정 사용 가능 여부 반환
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true; // true -> 사용 가능
    }
}
