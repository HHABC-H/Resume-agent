package com.h.resumeagent.common.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ForumCommentDTO {
    private Long id;
    private Long postId;
    private Long parentId;
    private Long authorId;
    private String authorName;
    private String content;
    private Integer likeCount;
    private Integer dislikeCount;
    private LocalDateTime createdAt;
}
