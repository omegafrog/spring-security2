package org.example.springsecurity.domain.post.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.springsecurity.domain.post.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDto {

    private long id;
    @JsonProperty("createdDatetime")
    private LocalDateTime createdDate;
    @JsonProperty("modifiedDatetime")
    private LocalDateTime modifiedDate;
    private String title;
    private long authorId;
    private String authorName;
    private boolean opened;
    private boolean listed;

    public PostDto(Post post) {
        this.id = post.getId();
        this.createdDate = post.getCreatedDate();
        this.modifiedDate = post.getModifiedDate();
        this.title = post.getTitle();
        this.authorId = post.getAuthor().getId();
        this.authorName = post.getAuthor().getNickname();
        this.opened = post.isOpened();
        this.listed = post.isListed();
    }

}
