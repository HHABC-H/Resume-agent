package com.h.resumeagent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "interview_question",
        indexes = {
                @Index(name = "idx_interview_question_session", columnList = "resume_session_id"),
                @Index(name = "idx_interview_question_type", columnList = "question_type")
        }
)
public class InterviewQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_session_id", nullable = false)
    private ResumeSessionEntity resumeSession;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", length = 80)
    private String questionType;

    @Column(name = "category", length = 120)
    private String category;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
