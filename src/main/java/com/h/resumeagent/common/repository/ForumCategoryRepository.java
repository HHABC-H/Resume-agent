package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumCategoryRepository extends JpaRepository<ForumCategoryEntity, Long> {

    @Modifying
    @Query("UPDATE ForumCategoryEntity c SET c.postCount = c.postCount + 1 WHERE c.id = :id")
    void incrementPostCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumCategoryEntity c SET c.postCount = c.postCount - 1 WHERE c.id = :id AND c.postCount > 0")
    void decrementPostCount(@Param("id") Long id);
}
