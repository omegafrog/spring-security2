package org.example.springsecurity.global.app;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.springsecurity.domain.member.member.entity.Member;
import org.example.springsecurity.domain.member.member.service.MemberService;
import org.example.springsecurity.global.Rq;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Rq rq;
    private final MemberService memberService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if(authorization == null){
            filterChain.doFilter(request, response);
            return;
        }

        if(!authorization.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = authorization.substring("Bearer ".length());

        Optional<Member> optionalMember = memberService.findByApiKey(apiKey);

        if(optionalMember.isEmpty()){
            filterChain.doFilter(request, response);
            return;
        }

        Member member = optionalMember.get();

        rq.setLogin(member.getUsername());

        filterChain.doFilter(request, response);
    }
}
