package net.watchbox.global.auth.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.watchbox.domain.account.entity.OauthAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
@Getter
public class CustomOAuth2User implements OAuth2User {
    private final OAuth2User delegate;
    private final OauthAccount oauthAccount;

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}

