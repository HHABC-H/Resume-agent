package com.h.resumeagent.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "evaluation_reference_answer",
        indexes = {
                @Index(name = "idx_eval_ref_answer_session", columnList = "resume_session_id"),
                @Index(name = "idx_eval_ref_answer_question_index", columnList = "question_index")
        }
)
public class EvaluationReferenceAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_session_id", nullable = false)
    private ResumeSessionEntity resumeSession;

    @Column(name = "question_index", nullable = false)
    private Integer questionIndex;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "reference_answer", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String referenceAnswer;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder.Default
    @OneToMany(mappedBy = "referenceAnswer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationReferenceKeyPointEntity> keyPoints = new ArrayList<>();
}
