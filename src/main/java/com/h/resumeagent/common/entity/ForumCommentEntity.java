package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_comment", indexes = {
    @Index(name = "idx_forum_comment_post", columnList = "post_id"),
    @Index(name = "idx_forum_comment_parent", columnList = "parent_id")
})
public class ForumCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "parent_id")
    @Builder.Default
    private Long parentId = 0L;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "dislike_count")
    @Builder.Default
    private Integer dislikeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private ForumPostEntity post;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (dislikeCount == null) dislikeCount = 0;
        if (parentId == null) parentId = 0L;
    }
}
