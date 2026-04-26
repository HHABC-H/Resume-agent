package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ArticleBookmarkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleBookmarkRepository extends JpaRepository<ArticleBookmarkEntity, Long> {
    Optional<ArticleBookmarkEntity> findByUserIdAndArticleId(Long userId, Long articleId);
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    Page<ArticleBookmarkEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
}