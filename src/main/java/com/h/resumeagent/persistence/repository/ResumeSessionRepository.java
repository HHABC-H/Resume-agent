package com.h.resumeagent.persistence.repository;

import com.h.resumeagent.persistence.entity.ResumeSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeSessionRepository extends JpaRepository<ResumeSessionEntity, Long> {

    Optional<ResumeSessionEntity> findByResumeId(String resumeId);
}
