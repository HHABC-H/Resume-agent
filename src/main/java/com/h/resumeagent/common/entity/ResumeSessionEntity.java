package com.h.resumeagent.common.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resume_session", indexes = {
                @Index(name = "idx_resume_session_user_id", columnList = "user_id"),
                @Index(name = "idx_resume_session_status", columnList = "status"),
                @Index(name = "idx_resume_session_position_type", columnList = "position_type"),
                @Index(name = "idx_resume_session_updated_at", columnList = "updated_at")
})
public class ResumeSessionEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "resume_id", nullable = false, unique = true, length = 64)
        private String resumeId;

        @Column(name = "resume_text", nullable = false, columnDefinition = "MEDIUMTEXT")
        private String resumeText;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        private ResumeStatus status;

        @Column(name = "position_type", nullable = false, length = 32)
        private String positionType;

        @Column(name = "resume_overall_score")
        private Integer resumeOverallScore;

        @Column(name = "score_project")
        private Integer scoreProject;

        @Column(name = "score_skill_match")
        private Integer scoreSkillMatch;

        @Column(name = "score_content")
        private Integer scoreContent;

        @Column(name = "score_structure")
        private Integer scoreStructure;

        @Column(name = "score_expression")
        private Integer scoreExpression;

        @Column(name = "resume_summary", columnDefinition = "TEXT")
        private String resumeSummary;

        @Column(name = "evaluation_session_id", length = 64)
        private String evaluationSessionId;

        @Column(name = "evaluation_total_questions")
        private Integer evaluationTotalQuestions;

        @Column(name = "evaluation_overall_score")
        private Integer evaluationOverallScore;

        @Column(name = "evaluation_overall_feedback", columnDefinition = "TEXT")
        private String evaluationOverallFeedback;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ResumeStrengthEntity> resumeStrengths = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ResumeSuggestionEntity> resumeSuggestions = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<InterviewQuestionEntity> interviewQuestions = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<EvaluationCategoryScoreEntity> evaluationCategoryScores = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<EvaluationQuestionDetailEntity> evaluationQuestionDetails = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<EvaluationStrengthEntity> evaluationStrengths = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<EvaluationImprovementEntity> evaluationImprovements = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "resumeSession", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<EvaluationReferenceAnswerEntity> evaluationReferenceAnswers = new ArrayList<>();
}
