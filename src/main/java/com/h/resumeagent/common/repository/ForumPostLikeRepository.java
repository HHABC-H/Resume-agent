package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumPostLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumPostLikeRepository extends JpaRepository<ForumPostLikeEntity, Long> {

    Optional<ForumPostLikeEntity> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Modifying
    @Query("DELETE FROM ForumPostLikeEntity p WHERE p.postId = :postId AND p.userId = :userId")
    void deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}