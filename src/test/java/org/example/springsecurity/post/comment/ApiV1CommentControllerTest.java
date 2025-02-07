package org.example.springsecurity.post.comment;

import org.example.springsecurity.domain.post.comment.controller.ApiV1CommentController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiV1CommentControllerTest {

    @Autowired
    private MockMvc mockmvc;

    @Test
    @DisplayName("댓글 작성")
    void write() throws Exception {
        Long postId = 1L;
        String content = "content";
        String apiKey = "user2";
        ResultActions perform = mockmvc.perform(
                post("/api/v1/posts/%d/comments".formatted(postId))
                        .content("""
                                {
                                    "content": "%s"
                                }
                                """
                                .stripIndent()
                                .formatted(content))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", "Bearer " + apiKey)
        );

        perform
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("201-1"));
    }

    @Test
    @DisplayName("댓글 조회")
    void getItem() throws Exception {
        Long postId = 1L;
        ResultActions perform = mockmvc
                .perform(
                        get("/api/v1/posts/%d/comments".formatted(postId))
                );

        perform
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(2));
    }
    @Test
    @DisplayName("댓글 수정")
    void modify() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;
        String apiKey = "user1";
        String content = "content";

        ResultActions perform = mockmvc
                .perform(
                        put("/api/v1/posts/%d/comments/%d"
                                .formatted(postId, commentId))
                                .header("Authorization", "Bearer " + apiKey)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """
                                        .stripIndent()
                                        .formatted(content))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                );

        perform
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글 수정이 완료되었습니다.".formatted(commentId)));
    }

    @Test
    @DisplayName("댓글 삭제")
    void delete1() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;
        String apiKey = "user1";

        ResultActions perform = mockmvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(postId, commentId))
                                .header("Authorization", "Bearer " + apiKey)
                );
        perform
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value(String.format("%d번 댓글 삭제가 완료되었습니다.", commentId)));
    }

}
