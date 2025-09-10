package com.beyond.HanSoom.common.config;

import com.beyond.HanSoom.common.auth.JwtAutenticationHandler;
import com.beyond.HanSoom.common.auth.JwtAuthorizationHandler;
import com.beyond.HanSoom.common.auth.JwtTokenFilter;
import com.beyond.HanSoom.common.auth.OriginRefererCsrfFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAutenticationHandler jwtAutenticationHandler;
    private final JwtAuthorizationHandler jwtAuthorizationHandler;
    private final OriginRefererCsrfFilter originRefererCsrfFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(c -> c.configurationSource(corsConfiguration()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(originRefererCsrfFilter, UsernamePasswordAuthenticationFilter.class)
                // token을 검증하고, token검증을 통해 Authentication 객체 생성
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(e ->
                        e.authenticationEntryPoint(jwtAutenticationHandler) // 401의 경우
                                .accessDeniedHandler(jwtAuthorizationHandler) // 403의 경우
                )

                .authorizeHttpRequests(a -> a.requestMatchers("/health", "/user/create", "/user/login", "/user/auth/refresh", "/user/google/login", "/user/google/reLogin", "/user/kakao/login", "/payment/**","/reservation/**", "/hotel/detail/**", "/hotel/list", "/hotel/nearby", "/connect/**", "/review/hotel/**", "/review/images/**", "/reply/hotels/**", "/review/ratings/**", "/hotel/popular", "hotel/place", "/hotel/suggest").permitAll().anyRequest().authenticated())
                .build();
    }
    // Todo - 프론트 연결
    private CorsConfigurationSource corsConfiguration(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://www.hansoom.shop"));
        configuration.setAllowedMethods(Arrays.asList("*")); // 모든 HTTP(get, post 등) 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더요소(Authorization 등) 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //모든 url패턴에 대해 cors설정 적용
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { // 객체만들고 AuthorService에 주입
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
