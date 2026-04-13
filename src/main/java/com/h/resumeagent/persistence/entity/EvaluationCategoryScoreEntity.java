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
        name = "evaluation_category_score",
        indexes = @Index(name = "idx_eval_category_session", columnList = "resume_session_id")
)
public class EvaluationCategoryScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_session_id", nullable = false)
    private ResumeSessionEntity resumeSession;

    @Column(name = "category", nullable = false, length = 120)
    private String category;

    @Column(name = "score")
    private Integer score;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
