package net.watchbox.domain.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.entity.OAuthProvider;
import net.watchbox.domain.auth.repository.OAuthAccountRepository;
import net.watchbox.domain.member.entity.Member;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthAccountService {
    private final OAuthAccountRepository oauthAccountRepository;

    @Transactional
    public OAuthAccount createOrUpdate(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email, name, oauthId;
        OAuthProvider oauthProvider;
        Optional<OAuthAccount> existingAccount;
        switch (provider) {
            case "google":
                oauthId = (String) attributes.get("sub");
                existingAccount = oauthAccountRepository.findByOauthId(oauthId);
                if(existingAccount.isPresent()) return existingAccount.get(); // ToDo: 업데이트 로직 추가 필요

                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                oauthProvider = OAuthProvider.GOOGLE;
                break;
            case "kakao":
                oauthId = attributes.get("id").toString();
                existingAccount = oauthAccountRepository.findByOauthId(oauthId);
                if(existingAccount.isPresent()) return existingAccount.get();

                Map attributesProperties = (Map) attributes.get("properties");
                name = (String) attributesProperties.get("nickname");
                Map attributesKakaoAcount = (Map) attributes.get("kakao_account");
                email = (String) attributesKakaoAcount.get("email");
                oauthProvider = OAuthProvider.KAKAO;
                break;
            case "naver": // ToDo: 네이버 파라미터 확인 필요
                Map attributesResponse = (Map) attributes.get("response");
                oauthId = attributesResponse.get("id").toString();
                existingAccount = oauthAccountRepository.findByOauthId(oauthId);
                if(existingAccount.isPresent()) return existingAccount.get();

                name = (String) attributesResponse.get("name");
                email = (String) attributesResponse.get("email");
                oauthProvider = OAuthProvider.NAVER;
                break;
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
        return oauthAccountRepository.save(OAuthAccount.builder()
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .email(email)
                .name(name)
                .build());
    }

    @Transactional
    public OAuthAccount createSampleAccount(String nickname, String email) {
        return oauthAccountRepository.save(OAuthAccount.builder()
                .oauthProvider(OAuthProvider.GOOGLE)
                .email(email)
                .oauthId("oauth_"+nickname)
                .name(nickname)
                .build());
    }

    /** OAuthAccount(FK 소유측)에 Member 연결 후 영속화. */
    @Transactional
    public void linkMember(OAuthAccount oauthAccount, Member member) {
        oauthAccount.linkMember(member);
        oauthAccountRepository.save(oauthAccount); // detached 면 merge 로 FK 반영
    }

    @Transactional
    public void deleteByMember(Member member) {
        oauthAccountRepository.findByMember(member)
                .ifPresent(oauthAccountRepository::delete);
    }

    public boolean existsByName(String name) {
        return oauthAccountRepository.existsByName(name);
    }
}
