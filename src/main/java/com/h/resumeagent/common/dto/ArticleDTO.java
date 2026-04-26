package com.h.resumeagent.common.dto;

import java.time.LocalDateTime;

public class ArticleDTO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String tags;
    private Long authorId;
    private String authorName;
    private Integer readCount;
    private String status;
    private Boolean isBookmarked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsBookmarked() { return isBookmarked; }
    public void setIsBookmarked(Boolean isBookmarked) { this.isBookmarked = isBookmarked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ArticleDTOBuilder builder() { return new ArticleDTOBuilder(); }

    public static class ArticleDTOBuilder {
        private Long id;
        private String title;
        private String content;
        private String category;
        private String tags;
        private Long authorId;
        private String authorName;
        private Integer readCount;
        private String status;
        private Boolean isBookmarked;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ArticleDTOBuilder id(Long id) { this.id = id; return this; }
        public ArticleDTOBuilder title(String title) { this.title = title; return this; }
        public ArticleDTOBuilder content(String content) { this.content = content; return this; }
        public ArticleDTOBuilder category(String category) { this.category = category; return this; }
        public ArticleDTOBuilder tags(String tags) { this.tags = tags; return this; }
        public ArticleDTOBuilder authorId(Long authorId) { this.authorId = authorId; return this; }
        public ArticleDTOBuilder authorName(String authorName) { this.authorName = authorName; return this; }
        public ArticleDTOBuilder readCount(Integer readCount) { this.readCount = readCount; return this; }
        public ArticleDTOBuilder status(String status) { this.status = status; return this; }
        public ArticleDTOBuilder isBookmarked(Boolean isBookmarked) { this.isBookmarked = isBookmarked; return this; }
        public ArticleDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ArticleDTOBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ArticleDTO build() {
            ArticleDTO dto = new ArticleDTO();
            dto.setId(id);
            dto.setTitle(title);
            dto.setContent(content);
            dto.setCategory(category);
            dto.setTags(tags);
            dto.setAuthorId(authorId);
            dto.setAuthorName(authorName);
            dto.setReadCount(readCount);
            dto.setStatus(status);
            dto.setIsBookmarked(isBookmarked);
            dto.setCreatedAt(createdAt);
            dto.setUpdatedAt(updatedAt);
            return dto;
        }
    }
}