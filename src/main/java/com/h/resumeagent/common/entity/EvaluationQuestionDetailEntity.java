package com.h.resumeagent.common.entity;

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
@Table(name = "evaluation_question_detail", indexes = {
                @Index(name = "idx_eval_detail_session", columnList = "resume_session_id"),
                @Index(name = "idx_eval_detail_question_index", columnList = "question_index")
})
public class EvaluationQuestionDetailEntity {

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

        @Column(name = "category", length = 120)
        private String category;

        @Column(name = "user_answer", columnDefinition = "MEDIUMTEXT")
        private String userAnswer;

        @Column(name = "score")
        private Integer score;

        @Column(name = "feedback", columnDefinition = "TEXT")
        private String feedback;
}
