package com.h.resumeagent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "v_resume_history")
public class ResumeHistoryViewEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "display_name")
    private String displayName;

    @Id
    @Column(name = "resume_id", length = 64, nullable = false)
    private String resumeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ResumeStatus status;

    @Column(name = "resume_overall_score")
    private Integer resumeOverallScore;

    @Column(name = "evaluation_overall_score")
    private Integer evaluationOverallScore;

    @Column(name = "question_count")
    private Long questionCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
