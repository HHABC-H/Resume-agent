package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {
    Page<ArticleEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<ArticleEntity> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status, Pageable pageable);
    Page<ArticleEntity> findByTitleContainingAndStatusOrderByCreatedAtDesc(String keyword, String status, Pageable pageable);
    List<String> findDistinctCategoryByStatus(String status);

    @Modifying
    @Query("UPDATE ArticleEntity a SET a.readCount = a.readCount + 1 WHERE a.id = :id")
    void incrementReadCount(@Param("id") Long id);
}