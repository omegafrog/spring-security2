package org.example.springsecurity.member;

import org.example.springsecurity.domain.member.member.controller.ApiV1MemberController;
import org.example.springsecurity.domain.member.member.entity.Member;
import org.example.springsecurity.domain.member.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    private static ResultActions checkMember(ResultActions resultActions, Member user1, String baseDataPath) throws Exception {
        return resultActions
                .andExpect(jsonPath(baseDataPath).exists())
                .andExpect(jsonPath(baseDataPath+".id").isNumber())
                .andExpect(jsonPath(baseDataPath+".nickname").value(user1.getNickname()))
                .andExpect(jsonPath(baseDataPath+".createdDate").exists())
                .andExpect(jsonPath(baseDataPath+".modifiedDate").exists());
    }

    @Test
    @DisplayName("회원 가입")
    void join() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .content("""
                                        {
                                            "username": "userNew",
                                            "password": "1234",
                                            "nickname": "무명"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        Member member = memberService.findByUsername("userNew").get();

        assertThat(member.getNickname()).isEqualTo("무명");

        checkMember(resultActions, member, "$.data")
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다."));

    }

    @Test
    @DisplayName("회원 가입2 - username이 이미 존재하는 케이스")
    void join2() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "1234",
                                            "nickname": "무명"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(status().isConflict())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.code").value("409-1"))
                .andExpect(jsonPath("$.msg").value("이미 사용중인 아이디입니다."));

    }

    @Test
    @DisplayName("로그인")
    void login() throws Exception {

        String username = "user1";
        String password = "user11234";

        ResultActions resultActions = loginRequest(username, password);

        Member user1 = memberService.findByUsername("user1").get();

        checkMember(resultActions, user1, "$.data.item")
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다.".formatted(user1.getNickname())))
                .andExpect(jsonPath("$.data.apiKey").exists());
    }

    @Test
    @DisplayName("로그인 실패")
    void loginFail() throws Exception {

        String username = "user1";
        String password = "user1";

        ResultActions resultActions = loginRequest(username, password);


        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.code").value("401-2"))
                .andExpect(jsonPath("$.msg").value("로그인 실패"));
    }


    @Test
    @DisplayName("존재하지 않는 username")
    void loginFail2() throws Exception {

        String username = "aaaa";
        String password = "user1";

        ResultActions resultActions = loginRequest(username, password);


        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 아이디입니다."));
    }

    @Test
    @DisplayName("username, password 누락")
    void loginFail3() throws Exception {

        String username = "";
        String password = "user1";

        ResultActions resultActions = loginRequest(username, password);


        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.code").value("400-1"))
                .andExpect(jsonPath("$.msg").value("username : NotBlank : must not be blank"));
    }
    private ResultActions loginRequest(String username, String password) throws Exception {
        return mvc
                .perform(
                        post("/api/v1/members/login")
                                .content("""
                                        {
                                            "username": "%s",
                                            "password": "%s"
                                        }
                                        """
                                        .formatted(username, password)
                                        .stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
    }
    @Test
    @DisplayName("유저 정보")
    void me() throws Exception {
        String apiKey = "user1";
        Member user1 = memberService.findByApiKey(apiKey).get();

        ResultActions resultActions= mvc
                .perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "Bearer "+apiKey));

        checkMember(resultActions, user1, "$.data")
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("유저 정보 조회 성공"));
    }


    @Test
    @DisplayName("유저 정보 - 잘못된 apikey")
    void me2() throws Exception {
        String apiKey = "user12";

        ResultActions resultActions= mvc
                .perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "Bearer "+apiKey));

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증 정보입니다."));
    }
}
