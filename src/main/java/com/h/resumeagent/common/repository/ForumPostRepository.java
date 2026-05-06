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

    @Query("SELECT p FROM ForumPostEntity p WHERE p.createdAt >= :startOfDay ORDER BY p.createdAt DESC")
    Page<ForumPostEntity> findTodayPosts(@Param("startOfDay") java.time.LocalDateTime startOfDay, Pageable pageable);

    Page<ForumPostEntity> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    Page<ForumPostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<ForumPostEntity> findByIsEssenceOrderByCreatedAtDesc(Boolean isEssence, Pageable pageable);

    Page<ForumPostEntity> findByIsTopOrderByCreatedAtDesc(Boolean isTop, Pageable pageable);

    @Query("SELECT p FROM ForumPostEntity p WHERE p.isEssence = true ORDER BY p.createdAt DESC")
    Page<ForumPostEntity> findEssences(Pageable pageable);

    @Query("SELECT p FROM ForumPostEntity p WHERE p.isEssence = true AND p.createdAt >= :startTime ORDER BY p.createdAt DESC")
    Page<ForumPostEntity> findEssencesSince(@Param("startTime") java.time.LocalDateTime startTime, Pageable pageable);

    Page<ForumPostEntity> findByOrderByViewCountDesc(Pageable pageable);

    @Query(value = "SELECT author_id, SUM(like_count) as total_likes FROM forum_post GROUP BY author_id ORDER BY total_likes DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findHotAuthors(@Param("limit") int limit);

    @Query(value = "SELECT author_id, SUM(like_count) as total_likes FROM forum_post WHERE created_at >= :startTime GROUP BY author_id ORDER BY total_likes DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findHotAuthorsSince(@Param("startTime") java.time.LocalDateTime startTime, @Param("limit") int limit);

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
    @Query("UPDATE ForumPostEntity p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.dislikeCount = p.dislikeCount - 1 WHERE p.id = :id AND p.dislikeCount > 0")
    void decrementDislikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.isEssence = :isEssence WHERE p.id = :id")
    void updateEssence(@Param("id") Long id, @Param("isEssence") Boolean isEssence);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.isTop = :isTop WHERE p.id = :id")
    void updateTop(@Param("id") Long id, @Param("isTop") Boolean isTop);

    long countByIsEssence(Boolean isEssence);

    long countByIsTop(Boolean isTop);

    Page<ForumPostEntity> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<ForumPostEntity> findByIsEssenceAndTitleContainingIgnoreCase(Boolean isEssence, String keyword, Pageable pageable);

    List<ForumPostEntity> findByAuthorId(Long authorId);

    @Query(value = "SELECT DISTINCT author_id FROM forum_post WHERE is_essence = true", nativeQuery = true)
    List<Long> findDistinctEssenceAuthors();

    @Query(value = "SELECT COALESCE(SUM(view_count), 0) FROM forum_post WHERE is_essence = true", nativeQuery = true)
    long sumViewsByEssence();

    @Query(value = "SELECT COALESCE(SUM(like_count), 0) FROM forum_post WHERE is_essence = true", nativeQuery = true)
    long sumLikesByEssence();

    @Query(value = "SELECT COALESCE(SUM(like_count), 0) FROM forum_post", nativeQuery = true)
    long sumLikes();

    @Query(value = "SELECT author_id, COUNT(*), COALESCE(SUM(like_count), 0) FROM forum_post GROUP BY author_id ORDER BY SUM(like_count) DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findHotAuthorsWithStats(@Param("limit") int limit);
}
