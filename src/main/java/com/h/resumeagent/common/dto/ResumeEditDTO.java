package com.h.resumeagent.common.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ResumeEditDTO {
    private Long id;
    private Long userId;
    private Map<String, Object> structuredData;
    private String resumeText;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Map<String, Object> getStructuredData() { return structuredData; }
    public void setStructuredData(Map<String, Object> structuredData) { this.structuredData = structuredData; }
    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ResumeEditDTOBuilder builder() { return new ResumeEditDTOBuilder(); }

    public static class ResumeEditDTOBuilder {
        private Long id;
        private Long userId;
        private Map<String, Object> structuredData;
        private String resumeText;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ResumeEditDTOBuilder id(Long id) { this.id = id; return this; }
        public ResumeEditDTOBuilder userId(Long userId) { this.userId = userId; return this; }
        public ResumeEditDTOBuilder structuredData(Map<String, Object> structuredData) { this.structuredData = structuredData; return this; }
        public ResumeEditDTOBuilder resumeText(String resumeText) { this.resumeText = resumeText; return this; }
        public ResumeEditDTOBuilder status(String status) { this.status = status; return this; }
        public ResumeEditDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ResumeEditDTOBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ResumeEditDTO build() {
            ResumeEditDTO dto = new ResumeEditDTO();
            dto.setId(id);
            dto.setUserId(userId);
            dto.setStructuredData(structuredData);
            dto.setResumeText(resumeText);
            dto.setStatus(status);
            dto.setCreatedAt(createdAt);
            dto.setUpdatedAt(updatedAt);
            return dto;
        }
    }
}