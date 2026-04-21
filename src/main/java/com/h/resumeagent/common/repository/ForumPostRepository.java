package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPostEntity, Long> {

    Page<ForumPostEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ForumPostEntity> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    Page<ForumPostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<ForumPostEntity> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    @Query("SELECT p FROM ForumPostEntity p WHERE p.status > 0 ORDER BY p.createdAt DESC")
    Page<ForumPostEntity> findEssences(Pageable pageable);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.dislikeCount = p.dislikeCount + 1 WHERE p.id = :id")
    void incrementDislikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.status = :status WHERE p.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
