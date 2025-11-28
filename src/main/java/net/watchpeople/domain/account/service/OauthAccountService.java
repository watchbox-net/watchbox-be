package net.watchpeople.domain.account.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.account.entity.OauthAccount;
import net.watchpeople.domain.account.entity.OauthProvider;
import net.watchpeople.domain.account.repository.OauthAccountRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OauthAccountService {
    private final OauthAccountRepository oauthAccountRepository;

    @Transactional
    public OauthAccount createOrUpdate(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email, name, oauthId;
        OauthProvider oauthProvider;
        Optional<OauthAccount> existingAccount;
        switch (provider) {
            case "google":
                oauthId = (String) attributes.get("sub");
                existingAccount = oauthAccountRepository.findByOauthId(oauthId);
                if(existingAccount.isPresent()) return existingAccount.get(); // ToDo: 업데이트 로직 추가 필요

                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                oauthProvider = OauthProvider.GOOGLE;
                break;
            case "kakao":
                oauthId = attributes.get("id").toString();
                existingAccount = oauthAccountRepository.findByOauthId(oauthId);
                if(existingAccount.isPresent()) return existingAccount.get();

                Map attributesProperties = (Map) attributes.get("properties");
                name = (String) attributesProperties.get("nickname");
                Map attributesKakaoAcount = (Map) attributes.get("kakao_account");
                email = (String) attributesKakaoAcount.get("email");
                oauthProvider = OauthProvider.KAKAO;
                break;
            case "naver": // ToDo: 네이버 파라미터 확인 필요
                Map attributesResponse = (Map) attributes.get("response");
                oauthId = attributesResponse.get("id").toString();
                existingAccount = oauthAccountRepository.findByOauthId(oauthId);
                if(existingAccount.isPresent()) return existingAccount.get();

                name = (String) attributesResponse.get("name");
                email = (String) attributesResponse.get("email");
                oauthProvider = OauthProvider.NAVER;
                break;
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
        return oauthAccountRepository.save(OauthAccount.builder()
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .email(email)
                .name(name)
                .build());
    }
}
