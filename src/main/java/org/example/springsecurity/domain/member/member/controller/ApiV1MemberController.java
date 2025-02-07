package org.example.springsecurity.domain.member.member.controller;

import org.example.springsecurity.domain.member.member.dto.MemberDto;
import org.example.springsecurity.domain.member.member.entity.Member;
import org.example.springsecurity.domain.member.member.service.MemberService;
import org.example.springsecurity.global.Rq;
import org.example.springsecurity.global.dto.RsData;
import org.example.springsecurity.global.exception.ServiceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberService memberService;
    private final Rq rq;

    record JoinReqBody(String username, String password, String nickname) {}

    @PostMapping("/join")
    public RsData<MemberDto> join(@RequestBody JoinReqBody reqBody) {

        memberService.findByUsername(reqBody.username())
                .ifPresent(_ -> {
                    throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                });


        Member member = memberService.join(reqBody.username(), reqBody.password(), reqBody.nickname());
        return new RsData<>(
                "201-1",
                "회원 가입이 완료되었습니다.",
                new MemberDto(member)
                );
    }

    public record LoginResBody(MemberDto item, String apiKey){}

    public record LoginReqBody(@NotBlank String username, @NotBlank String password){}

    @PostMapping("/login")
    public RsData<LoginResBody> login(@Valid @RequestBody LoginReqBody reqBody) {
        Member member = memberService.login(reqBody.username, reqBody.password);

        return new RsData<>("200-1", "%s님 환영합니다.".formatted(member.getNickname()),
                new LoginResBody(new MemberDto(member), member.getApiKey()));
    }

    @GetMapping("/me")
    public RsData<MemberDto> me() {

        Member member = rq.getAuthenticatedActor();

        return new RsData<>("200-1", "유저 정보 조회 성공", new MemberDto(member));
    }
}
