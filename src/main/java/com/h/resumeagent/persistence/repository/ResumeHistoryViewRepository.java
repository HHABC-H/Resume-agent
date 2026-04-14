package com.h.resumeagent.persistence.repository;

import com.h.resumeagent.persistence.entity.ResumeHistoryViewEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeHistoryViewRepository extends JpaRepository<ResumeHistoryViewEntity, String> {

    List<ResumeHistoryViewEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<ResumeHistoryViewEntity> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
