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
import org.springframework.http.ResponseEntity;
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
    public String index(@RequestParam(defaultValue = "0") int page, 
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPostDTO> posts = forumService.getPosts(pageable);
        List<ForumCategoryDTO> categories = forumService.getCategories();
        
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categories);
        return "forum/index";
    }

    @GetMapping("/post/{id}")
    public String postDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        ForumPostDetailDTO post = forumService.getPostDetail(id, userId);
        forumService.incrementViewCount(id);
        
        model.addAttribute("post", post);
        return "forum/post-detail";
    }

    @GetMapping("/publish")
    public String publishPage(Model model) {
        List<ForumCategoryDTO> categories = forumService.getCategories();
        model.addAttribute("categories", categories);
        return "forum/publish";
    }

    @PostMapping("/post")
    @ResponseBody
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        ForumPostDTO post = forumService.createPost(request, userId);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/post/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            forumService.deletePost(id, userId);
            return ResponseEntity.ok(Map.of("message", "Post deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/post/{id}/like")
    @ResponseBody
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        forumService.likePost(id);
        return ResponseEntity.ok(Map.of("message", "Liked"));
    }

    @PostMapping("/post/{id}/dislike")
    @ResponseBody
    public ResponseEntity<?> dislikePost(@PathVariable Long id) {
        forumService.dislikePost(id);
        return ResponseEntity.ok(Map.of("message", "Disliked"));
    }

    @PostMapping("/comment")
    @ResponseBody
    public ResponseEntity<?> createComment(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        Long postId = Long.valueOf(payload.get("postId").toString());
        Long parentId = payload.get("parentId") != null ? Long.valueOf(payload.get("parentId").toString()) : null;
        String content = payload.get("content").toString();
        
        ForumCommentDTO comment = forumService.createComment(postId, parentId, content, userId);
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/comment/{id}/like")
    @ResponseBody
    public ResponseEntity<?> likeComment(@PathVariable Long id) {
        forumService.likeComment(id);
        return ResponseEntity.ok(Map.of("message", "Liked"));
    }

    @PostMapping("/comment/{id}/dislike")
    @ResponseBody
    public ResponseEntity<?> dislikeComment(@PathVariable Long id) {
        forumService.dislikeComment(id);
        return ResponseEntity.ok(Map.of("message", "Disliked"));
    }

    @GetMapping("/category/{id}")
    public String category(@PathVariable Long id,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPostDTO> posts = forumService.getPostsByCategory(id, pageable);
        List<ForumCategoryDTO> categories = forumService.getCategories();
        
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categories);
        model.addAttribute("currentCategoryId", id);
        return "forum/category";
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

    @PostMapping("/admin/essence/{id}")
    @ResponseBody
    public ResponseEntity<?> setEssence(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        forumService.setEssence(id, userId);
        return ResponseEntity.ok(Map.of("message", "Essence set"));
    }

    @DeleteMapping("/admin/essence/{id}")
    @ResponseBody
    public ResponseEntity<?> removeEssence(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        forumService.removeEssence(id);
        return ResponseEntity.ok(Map.of("message", "Essence removed"));
    }

    @PostMapping("/admin/top/{id}")
    @ResponseBody
    public ResponseEntity<?> setTop(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        forumService.setTop(id, userId);
        return ResponseEntity.ok(Map.of("message", "Top set"));
    }

    @DeleteMapping("/admin/top/{id}")
    @ResponseBody
    public ResponseEntity<?> removeTop(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null || !isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        forumService.removeTop(id);
        return ResponseEntity.ok(Map.of("message", "Top removed"));
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
