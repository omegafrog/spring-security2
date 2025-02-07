package org.example.springsecurity.domain.post.post.dto;

import org.example.springsecurity.domain.post.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
public class PageDto {
    private List<PostDto> items;
    private long totalElementSize;
    private int totalPageNum;
    private int currentPageNum;

    public PageDto(Page<Post> page) {
        this.items=page.getContent().stream().map(PostDto::new).toList();
        this.totalElementSize = page.getTotalElements();
        this.totalPageNum = page.getTotalPages();
        this.currentPageNum = page.getNumber();
        this.pageSize = page.getSize();
    }

    private int pageSize;
}
