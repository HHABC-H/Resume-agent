package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_post_tag")
@IdClass(ForumPostTagEntity.ForumPostTagId.class)
public class ForumPostTagEntity {
    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "tag_id")
    private Long tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private ForumPostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private ForumTagEntity tag;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForumPostTagId implements Serializable {
        private Long postId;
        private Long tagId;
    }
}
