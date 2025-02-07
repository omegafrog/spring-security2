package org.example.springsecurity.global.app;

import org.example.springsecurity.domain.member.member.service.MemberService;
import org.example.springsecurity.global.Rq;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, MemberService memberService, Rq rq) throws Exception {
        http
                .authorizeHttpRequests(
                        req -> req
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/*/posts/{id:\\d+}", "/api/*/posts", "/api/*/posts/{id:\\d+}", "/api/*/posts/{id:\\d+}/comments")
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new CustomAuthenticationFilter(rq, memberService), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers ->
                        headers.addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)));
        return http.build();
    }
}
