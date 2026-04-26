package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resume_edit_session", indexes = {
    @Index(name = "idx_resume_edit_session_user_id", columnList = "user_id"),
    @Index(name = "idx_resume_edit_session_status", columnList = "status")
})
public class ResumeEditSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "structured_data", nullable = false, columnDefinition = "JSON")
    private String structuredData;

    @Column(name = "resume_text", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String resumeText;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}