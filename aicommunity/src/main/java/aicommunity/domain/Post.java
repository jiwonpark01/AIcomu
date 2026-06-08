package aicommunity.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Post {
    private Long id;
    private String title;
    private String content;
    private Category category;
    private Long authorId;        // users.id 참조 (FK)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
