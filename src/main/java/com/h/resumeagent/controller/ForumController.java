package com.h.resumeagent.controller;

import com.h.resumeagent.common.dto.*;
import com.h.resumeagent.interceptor.AuthTokenInterceptor;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.service.ForumService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/forum")
public class ForumController {

    private final ForumService forumService;
    private final AuthService authService;

    public ForumController(ForumService forumService, AuthService authService) {
        this.forumService = forumService;
        this.authService = authService;
    }

    @GetMapping
    @ResponseBody
    public ApiResponse<Page<ForumPostDTO>> getPosts(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPostDTO> posts = forumService.getPosts(pageable);
        return ApiResponse.success(posts);
    }

    @GetMapping("/post/{id}")
    public String postDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        ForumPostDetailDTO post = forumService.getPostDetail(id, userId);
        forumService.incrementViewCount(id);

        model.addAttribute("post", post);
        return "forum/post-detail";
    }

    @GetMapping("/post/{id}/detail")
    @ResponseBody
    public ApiResponse<ForumPostDetailDTO> getPostDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        ForumPostDetailDTO post = forumService.getPostDetail(id, userId);
        forumService.incrementViewCount(id);
        return ApiResponse.success(post);
    }

    @GetMapping("/publish")
    public String publishPage(Model model) {
        List<ForumCategoryDTO> categories = forumService.getCategories();
        model.addAttribute("categories", categories);
        return "forum/publish";
    }

    @PostMapping("/post")
    @ResponseBody
    public ApiResponse<ForumPostDTO> createPost(@RequestBody CreatePostRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }

        ForumPostDTO post = forumService.createPost(request, userId);
        return ApiResponse.success("发布成功", post);
    }

    @DeleteMapping("/post/{id}")
    @ResponseBody
    public ApiResponse<Void> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }

        try {
            forumService.deletePost(id, userId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/post/{id}/like")
    @ResponseBody
    public ApiResponse<Void> likePost(@PathVariable Long id) {
        forumService.likePost(id);
        return ApiResponse.success("点赞成功", null);
    }

    @PostMapping("/post/{id}/dislike")
    @ResponseBody
    public ApiResponse<Void> dislikePost(@PathVariable Long id) {
        forumService.dislikePost(id);
        return ApiResponse.success("点踩成功", null);
    }

    @PostMapping("/comment")
    @ResponseBody
    public ApiResponse<ForumCommentDTO> createComment(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }

        Long postId = Long.valueOf(payload.get("postId").toString());
        Long parentId = payload.get("parentId") != null ? Long.valueOf(payload.get("parentId").toString()) : null;
        String content = payload.get("content").toString();

        ForumCommentDTO comment = forumService.createComment(postId, parentId, content, userId);
        return ApiResponse.success("评论成功", comment);
    }

    @PostMapping("/comment/{id}/like")
    @ResponseBody
    public ApiResponse<Void> likeComment(@PathVariable Long id) {
        forumService.likeComment(id);
        return ApiResponse.success("点赞成功", null);
    }

    @PostMapping("/comment/{id}/dislike")
    @ResponseBody
    public ApiResponse<Void> dislikeComment(@PathVariable Long id) {
        forumService.dislikeComment(id);
        return ApiResponse.success("点踩成功", null);
    }

    @DeleteMapping("/comment/{id}/like")
    @ResponseBody
    public ApiResponse<Void> unlikeComment(@PathVariable Long id) {
        forumService.unlikeComment(id);
        return ApiResponse.success("取消点赞成功", null);
    }

    @DeleteMapping("/comment/{id}/dislike")
    @ResponseBody
    public ApiResponse<Void> undislikeComment(@PathVariable Long id) {
        forumService.undislikeComment(id);
        return ApiResponse.success("取消点踩成功", null);
    }

    @GetMapping("/category/{id}")
    @ResponseBody
    public ApiResponse<Page<ForumPostDTO>> getPostsByCategory(@PathVariable Long id,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPostDTO> posts = forumService.getPostsByCategory(id, pageable);
        return ApiResponse.success(posts);
    }

    @GetMapping("/essences")
    public String essences(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPostDTO> posts = forumService.getEssences(pageable);
        List<ForumCategoryDTO> categories = forumService.getCategories();
        
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categories);
        return "forum/essences";
    }

    @GetMapping("/categories")
    @ResponseBody
    public ApiResponse<List<ForumCategoryDTO>> getCategories() {
        return ApiResponse.success(forumService.getCategories());
    }

    @GetMapping("/hot")
    @ResponseBody
    public ApiResponse<Page<ForumPostDTO>> getHotPosts(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(forumService.getHotPosts(PageRequest.of(page, size)));
    }

    @GetMapping("/hot-authors")
    @ResponseBody
    public ApiResponse<List<HotAuthorDTO>> getHotAuthors(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(forumService.getHotAuthors(limit));
    }

    @GetMapping("/author/{id}/posts")
    @ResponseBody
    public ApiResponse<Page<ForumPostDTO>> getAuthorPosts(@PathVariable Long id,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(forumService.getPostsByAuthor(id, pageable));
    }

    @PostMapping("/admin/essence/{id}")
    @ResponseBody
    public ApiResponse<Void> setEssence(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ApiResponse.error(403, "无权限操作");
        }

        forumService.setEssence(id, userId);
        return ApiResponse.success("设置精华成功", null);
    }

    @DeleteMapping("/admin/essence/{id}")
    @ResponseBody
    public ApiResponse<Void> removeEssence(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ApiResponse.error(403, "无权限操作");
        }

        forumService.removeEssence(id);
        return ApiResponse.success("取消精华成功", null);
    }

    @PostMapping("/admin/top/{id}")
    @ResponseBody
    public ApiResponse<Void> setTop(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ApiResponse.error(403, "无权限操作");
        }

        forumService.setTop(id, userId);
        return ApiResponse.success("置顶成功", null);
    }

    @DeleteMapping("/admin/top/{id}")
    @ResponseBody
    public ApiResponse<Void> removeTop(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ApiResponse.error(403, "无权限操作");
        }

        forumService.removeTop(id);
        return ApiResponse.success("取消置顶成功", null);
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
                .map(user -> "ROLE_ADMIN".equals(user.role()))
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
