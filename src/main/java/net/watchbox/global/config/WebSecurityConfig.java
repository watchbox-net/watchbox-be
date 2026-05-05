package net.watchbox.global.config;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.global.auth.jwt.TokenAuthenticationFilter;
import net.watchbox.domain.auth.repository.RefreshTokenRepository;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.auth.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import net.watchbox.global.auth.oauth.OAuth2SuccessHandler;
import net.watchbox.global.auth.oauth.OAuth2UserCustomService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final TokenProvider tokenProvider;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final RefreshTokenRepository refreshTokenRepository;

    private final MemberService memberService;
    private final BoxService boxService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable) // 클라이언트 측에서 로그아웃을 처리하는 대신에 인증 서버에 로그아웃 요청을 전달하여 세션을 종료하고 토큰을 무효화한다.
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                        ).permitAll()
//                        .anyRequest().authenticated())
                        .anyRequest().permitAll()) // 일단 전체 오픈 ToDo: 인증 필수/제외할 엔드포인트 설정
                .oauth2Login(oauth2 -> oauth2
                                // Authorization 요청과 관련된 상태 저장 | (url 기본값: /oauth2/authorization/{registrationId})
                                .authorizationEndpoint(authorizationEndpoint
                                        -> authorizationEndpoint.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))
                                .userInfoEndpoint(userInfoEndpoint
                                        -> userInfoEndpoint.userService(oAuth2UserCustomService))
                                .successHandler(oAuth2SuccessHandler())
//                        .successHandler(oAuth2SuccessHandler)
                )
                .logout(logout -> logout // 로그아웃 설정
//                        .logoutSuccessUrl("/") // 로그아웃 시 이동 URL
                                .invalidateHttpSession(true) // 로그아웃 이후 세션을 전체 삭제할지 여부
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
//                                new AntPathRequestMatcher("/api/**")
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**")
                        )
                )
                .build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(
                tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                memberService,
                boxService
        );
    }
}
