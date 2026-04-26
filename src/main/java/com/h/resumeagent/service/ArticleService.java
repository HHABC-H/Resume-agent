package com.h.resumeagent.service;

import com.h.resumeagent.common.dto.ArticleDTO;
import com.h.resumeagent.common.dto.CreateArticleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleService {
    Page<ArticleDTO> getArticles(String category, String keyword, Pageable pageable);
    ArticleDTO getArticleDetail(Long id, Long userId);
    List<String> getCategories();
    ArticleDTO createArticle(CreateArticleRequest request, Long authorId);
    ArticleDTO updateArticle(Long id, CreateArticleRequest request, Long operatorId);
    void deleteArticle(Long id);
    void toggleBookmark(Long articleId, Long userId);
    Page<ArticleDTO> getBookmarks(Long userId, Pageable pageable);
    boolean isBookmarked(Long articleId, Long userId);
}