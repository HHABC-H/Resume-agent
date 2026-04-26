package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ResumeEditSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeEditSessionRepository extends JpaRepository<ResumeEditSessionEntity, Long> {
    Optional<ResumeEditSessionEntity> findByUserIdAndStatus(Long userId, String status);
    Page<ResumeEditSessionEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);
    Page<ResumeEditSessionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}