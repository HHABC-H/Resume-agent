package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumPostBookmarkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumPostBookmarkRepository extends JpaRepository<ForumPostBookmarkEntity, Long> {

    Optional<ForumPostBookmarkEntity> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Page<ForumPostBookmarkEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByPostIdAndUserId(Long postId, Long userId);
}