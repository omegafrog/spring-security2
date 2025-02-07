package org.example.springsecurity.domain.post.post.controller;

import org.example.springsecurity.domain.member.member.entity.Member;
import org.example.springsecurity.domain.member.member.service.MemberService;
import org.example.springsecurity.domain.post.post.dto.PageDto;
import org.example.springsecurity.domain.post.post.dto.PostWithContentDto;
import org.example.springsecurity.domain.post.post.entity.Post;
import org.example.springsecurity.domain.post.post.service.PostService;
import org.example.springsecurity.global.Rq;
import org.example.springsecurity.global.dto.RsData;
import org.example.springsecurity.global.exception.ServiceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;
    private final Rq rq;
    private final MemberService memberService;


    @GetMapping
    public RsData<PageDto> getItems(@RequestParam(name = "page", defaultValue = "0") int currentPageNum,
                                    @RequestParam(name = "size", defaultValue = "5") int pageSize,
                                    @RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "keyword-type", required = false, defaultValue = "title") String keywordType) {

        Page<Post> posts;
        if (keyword != null && keywordType.equals("title"))
            posts = postService.searchItemByTitle(currentPageNum, pageSize, keyword);
        else if (keyword != null && keywordType.equals("content"))
            posts = postService.searchItemByContent(currentPageNum, pageSize, keyword);
        else
            posts = postService.getItems(currentPageNum, pageSize);

        return new RsData<>(
                "200-1",
                "글 목록 조회가 완료되었습니다.",
                new PageDto(posts)
        );
    }

    @GetMapping("/me")
    public RsData<PageDto> getMines(@RequestParam(name = "page", defaultValue = "0") int currentPageNum,
                                      @RequestParam(name = "size", defaultValue = "5") int pageSize,
                                      @RequestParam(name = "keyword", required = false) String keyword,
                                      @RequestParam(name = "keyword-type", required = false, defaultValue = "title") String keywordType) {

        Member member = rq.getAuthenticatedActor();

        Page<Post> posts;
        if(keyword != null && keywordType.equals("title"))
            posts = postService.getMinesByTitle(member, currentPageNum, pageSize, keyword);
        else if (keyword != null && keywordType.equals("content"))
            posts = postService.getMinesByContent(member, currentPageNum, pageSize, keyword);
        else
            posts = postService.getMines(member, currentPageNum, pageSize);

        return new RsData<>(
                "200-1",
                "내 글 조회 성공.",
                new PageDto(posts)
        );
    }


    @GetMapping("{id}")
    public RsData<PostWithContentDto> getItem(@PathVariable long id) {

        Post post = postService.getItem(id)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 글입니다."));

        if (!post.isOpened()) {
            Member member = rq.getAuthenticatedActor();
            post.canRead(member);
        }

        return new RsData<>(
                "200-1",
                "글 조회가 완료되었습니다.",
                new PostWithContentDto(post)
        );
    }

    @DeleteMapping("/{id}")
    public RsData<Void> delete(@PathVariable long id) {

        Member actor = rq.getAuthenticatedActor();
        Post post = postService.getItem(id).get();

        post.canDelete(actor);
        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글 삭제가 완료되었습니다.".formatted(id)
        );
    }


    record ModifyReqBody(@NotBlank @Length(min = 3) String title,
                         @NotBlank @Length(min = 3) String content,
                         boolean opened,
                         boolean listed) {
    }

    @PutMapping("{id}")
    public RsData<PostWithContentDto> modify(@PathVariable long id, @RequestBody @Valid ModifyReqBody body
    ) {

        Member actor = rq.getAuthenticatedActor();
        Post post = postService.getItem(id).get();

        if (post.getAuthor().getId() != actor.getId()) {
            throw new ServiceException("403-1", "자신이 작성한 글만 수정 가능합니다.");
        }

        post.canModify(actor);
        Post modify = postService.modify(post, body.title(), body.content(), body.opened, body.listed);
        return new RsData<>(
                "200-1",
                "%d번 글 수정이 완료되었습니다.".formatted(id),
                new PostWithContentDto(modify)
        );
    }

    record WriteReqBody(
            @NotBlank @Length(min = 3) String title,
            @NotBlank @Length(min = 3) String content,
            boolean opened,
            boolean listed
    ) {
    }

    @PostMapping
    public RsData<PostWithContentDto> write(@RequestBody @Valid WriteReqBody body,
                                            @AuthenticationPrincipal UserDetails principal) {

        Member actor = memberService.findByUsername(principal.getUsername()).get();

        Post post = postService.write(actor, body.title(), body.content(), body.opened, body.listed);

        return new RsData<>(
                "200-1",
                "글 작성이 완료되었습니다.",
                new PostWithContentDto(post)
        );
    }
}
