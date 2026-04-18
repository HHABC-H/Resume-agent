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
@Table(name = "resume_suggestion", indexes = @Index(name = "idx_resume_suggestion_session", columnList = "resume_session_id"))
public class ResumeSuggestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_session_id", nullable = false)
    private ResumeSessionEntity resumeSession;

    @Column(name = "category", length = 120)
    private String category;

    @Column(name = "priority_level", length = 60)
    private String priorityLevel;

    @Column(name = "issue_text", length = 1000)
    private String issueText;

    @Column(name = "recommendation", length = 2000)
    private String recommendation;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
