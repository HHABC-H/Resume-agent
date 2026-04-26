package com.h.resumeagent.service.impl;

import com.h.resumeagent.common.dto.ArticleDTO;
import com.h.resumeagent.common.dto.CreateArticleRequest;
import com.h.resumeagent.common.entity.ArticleBookmarkEntity;
import com.h.resumeagent.common.entity.ArticleEntity;
import com.h.resumeagent.common.repository.ArticleBookmarkRepository;
import com.h.resumeagent.common.repository.ArticleRepository;
import com.h.resumeagent.common.repository.UserRepository;
import com.h.resumeagent.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleBookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository,
                              ArticleBookmarkRepository bookmarkRepository,
                              UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<ArticleDTO> getArticles(String category, String keyword, Pageable pageable) {
        Page<ArticleEntity> page;
        if (keyword != null && !keyword.isEmpty()) {
            page = articleRepository.findByTitleContainingAndStatusOrderByCreatedAtDesc(keyword, "PUBLISHED", pageable);
        } else if (category != null && !category.isEmpty() && !"all".equalsIgnoreCase(category)) {
            page = articleRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, "PUBLISHED", pageable);
        } else {
            page = articleRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED", pageable);
        }
        return page.map(this::toDTO);
    }

    @Override
    public ArticleDTO getArticleDetail(Long id, Long userId) {
        ArticleEntity entity = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        articleRepository.incrementReadCount(id);
        ArticleDTO dto = toDTO(entity);
        if (userId != null) {
            dto.setIsBookmarked(bookmarkRepository.existsByUserIdAndArticleId(userId, id));
        }
        return dto;
    }

    @Override
    public List<String> getCategories() {
        List<String> categories = articleRepository.findDistinctCategoryByStatus("PUBLISHED");
        if (categories.isEmpty()) {
            categories = Arrays.asList("Java", "Python", "Frontend", "DB", "架构", "面试技巧");
        }
        return categories;
    }

    @Override
    public ArticleDTO createArticle(CreateArticleRequest request, Long authorId) {
        ArticleEntity entity = ArticleEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .tags(request.getTags())
                .authorId(authorId)
                .status("PUBLISHED")
                .build();
        entity = articleRepository.save(entity);
        return toDTO(entity);
    }

    @Override
    public ArticleDTO updateArticle(Long id, CreateArticleRequest request, Long operatorId) {
        ArticleEntity entity = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setCategory(request.getCategory());
        entity.setTags(request.getTags());
        entity = articleRepository.save(entity);
        return toDTO(entity);
    }

    @Override
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    public void toggleBookmark(Long articleId, Long userId) {
        if (bookmarkRepository.existsByUserIdAndArticleId(userId, articleId)) {
            bookmarkRepository.deleteByUserIdAndArticleId(userId, articleId);
        } else {
            ArticleBookmarkEntity bookmark = ArticleBookmarkEntity.builder()
                    .userId(userId)
                    .articleId(articleId)
                    .build();
            bookmarkRepository.save(bookmark);
        }
    }

    @Override
    public Page<ArticleDTO> getBookmarks(Long userId, Pageable pageable) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(b -> articleRepository.findById(b.getArticleId()).map(this::toDTO).orElse(null));
    }

    @Override
    public boolean isBookmarked(Long articleId, Long userId) {
        return bookmarkRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    private ArticleDTO toDTO(ArticleEntity entity) {
        String authorName = userRepository.findById(entity.getAuthorId())
                .map(u -> u.getDisplayName() != null ? u.getDisplayName() : u.getUsername())
                .orElse("Unknown");

        return ArticleDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .category(entity.getCategory())
                .tags(entity.getTags())
                .authorId(entity.getAuthorId())
                .authorName(authorName)
                .readCount(entity.getReadCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}