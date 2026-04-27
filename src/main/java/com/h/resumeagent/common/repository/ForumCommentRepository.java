package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumCommentEntity, Long> {

    List<ForumCommentEntity> findByPostIdAndParentIdOrderByCreatedAtAsc(Long postId, Long parentId);

    List<ForumCommentEntity> findByPostIdOrderByCreatedAtAsc(Long postId);

    @Modifying
    @Query("UPDATE ForumCommentEntity c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumCommentEntity c SET c.dislikeCount = c.dislikeCount + 1 WHERE c.id = :id")
    void incrementDislikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumCommentEntity c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumCommentEntity c SET c.dislikeCount = c.dislikeCount - 1 WHERE c.id = :id AND c.dislikeCount > 0")
    void decrementDislikeCount(@Param("id") Long id);
}
