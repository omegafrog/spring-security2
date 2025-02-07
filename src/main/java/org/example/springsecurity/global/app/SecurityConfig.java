package org.example.springsecurity.global.app;

import org.example.springsecurity.domain.member.member.service.MemberService;
import org.example.springsecurity.global.Rq;
import org.example.springsecurity.global.dto.RsData;
import org.example.springsecurity.util.Util;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                                .requestMatchers(HttpMethod.POST,
                                        "/api/*/members/login", "/api/*/members/join")
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new CustomAuthenticationFilter(rq, memberService), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers ->
                        headers.addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, ex) -> {
                                    response.setContentType("application/json;charset=utf-8");
                                    response.setStatus(401);
                                    response.getWriter().write(
                                            Util.Json.toJson(
                                                    new RsData<>(
                                                            "401-1",
                                                            "인증이 필요합니다."
                                                    )
                                            )
                                    );
                                }
                        ));
        return http.build();
    }
}
