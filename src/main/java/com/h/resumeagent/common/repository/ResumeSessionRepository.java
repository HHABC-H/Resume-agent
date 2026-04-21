package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ResumeSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeSessionRepository extends JpaRepository<ResumeSessionEntity, Long> {

    Optional<ResumeSessionEntity> findByResumeId(String resumeId);

    List<ResumeSessionEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<ResumeSessionEntity> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    @Query("select count(q.id) from InterviewQuestionEntity q where q.resumeSession.id = :sessionId")
    long countInterviewQuestionsBySessionId(@Param("sessionId") Long sessionId);
}
