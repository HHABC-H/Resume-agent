package com.h.resumeagent.common.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ForumPostDetailDTO {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private Integer status;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ForumCommentDTO> comments;
}
