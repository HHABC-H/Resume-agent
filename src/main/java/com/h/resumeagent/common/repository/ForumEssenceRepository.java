package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumEssenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumEssenceRepository extends JpaRepository<ForumEssenceEntity, Long> {

    Optional<ForumEssenceEntity> findByPostId(Long postId);

    boolean existsByPostId(Long postId);
}
