package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_category")
public class ForumCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "post_count")
    private Integer postCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = java.time.LocalDateTime.now();
        if (postCount == null) postCount = 0;
        if (sortOrder == null) sortOrder = 0;
    }
}
