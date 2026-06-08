package aicommunity.dto.post;

import aicommunity.domain.Category;
import aicommunity.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final Category category;
    private final Long authorId;
    private final String authorNickname;  // 화면에 보여줄 작성자 이름
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Post 도메인 + 작성자 닉네임을 받아서 DTO 생성
    public PostResponse(Post post, String authorNickname) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.authorId = post.getAuthorId();
        this.authorNickname = authorNickname;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}
