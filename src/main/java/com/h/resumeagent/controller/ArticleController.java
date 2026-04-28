package com.h.resumeagent.controller;

import com.h.resumeagent.common.dto.ApiResponse;
import com.h.resumeagent.common.dto.ArticleDTO;
import com.h.resumeagent.common.dto.CreateArticleRequest;
import com.h.resumeagent.interceptor.AuthTokenInterceptor;
import com.h.resumeagent.service.ArticleService;
import com.h.resumeagent.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;
    private final AuthService authService;

    public ArticleController(ArticleService articleService, AuthService authService) {
        this.articleService = articleService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<Page<ArticleDTO>> getArticles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleDTO> result = articleService.getArticles(category, keyword, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleDTO> getArticleDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        ArticleDTO dto = articleService.getArticleDetail(id, userId);
        return ApiResponse.success(dto);
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> getCategories() {
        return ApiResponse.success(articleService.getCategories());
    }

    @GetMapping("/bookmark")
    public ApiResponse<Page<ArticleDTO>> getBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(articleService.getBookmarks(userId, pageable));
    }

    @PostMapping("/{id}/bookmark")
    public ApiResponse<Void> toggleBookmark(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        articleService.toggleBookmark(id, userId);
        return ApiResponse.success("操作成功", null);
    }

    @PostMapping
    public ApiResponse<ArticleDTO> createArticle(@RequestBody CreateArticleRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        if (!isAdmin(httpRequest)) {
            return ApiResponse.error(403, "无权限操作");
        }
        ArticleDTO dto = articleService.createArticle(request, userId);
        return ApiResponse.success("创建成功", dto);
    }

    @PutMapping("/{id}")
    public ApiResponse<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody CreateArticleRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        if (!isAdmin(httpRequest)) {
            return ApiResponse.error(403, "无权限操作");
        }
        ArticleDTO dto = articleService.updateArticle(id, request, userId);
        return ApiResponse.success("更新成功", dto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        if (!isAdmin(request)) {
            return ApiResponse.error(403, "无权限操作");
        }
        articleService.deleteArticle(id);
        return ApiResponse.success("删除成功", null);
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute(AuthTokenInterceptor.CURRENT_USER_ID_ATTR);
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return false;
        return authService.authenticate(resolveToken(request))
                .map(user -> "ADMIN".equals(user.role()))
                .orElse(false);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("RA_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}