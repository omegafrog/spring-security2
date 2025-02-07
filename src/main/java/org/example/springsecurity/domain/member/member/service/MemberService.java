package org.example.springsecurity.domain.member.member.service;

import org.example.springsecurity.domain.member.member.entity.Member;
import org.example.springsecurity.domain.member.member.repository.MemberRepository;
import org.example.springsecurity.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member join(String username, String password, String nickname) {

        Member member = Member.builder()
                .username(username)
                .password(password)
                .apiKey(username)
                .nickname(nickname)
                .build();

        return memberRepository.save(member);
    }

    public long count() {
        return memberRepository.count();
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public Member login(String username, String password) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("401-1", "잘못된 아이디입니다."));

        if(!member.getPassword().equals(password)) {
           throw new ServiceException("401-2", "로그인 실패");
        }
        return member;
    }
}