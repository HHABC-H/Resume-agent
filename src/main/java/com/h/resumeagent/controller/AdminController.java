package com.h.resumeagent.controller;

import com.h.resumeagent.common.entity.UserEntity;
import com.h.resumeagent.common.repository.ResumeHistoryViewRepository;
import com.h.resumeagent.common.repository.UserRepository;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.service.MockInterviewService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final MockInterviewService interviewService;
    private final ResumeHistoryViewRepository resumeHistoryViewRepository;
    private final UserRepository userRepository;

    @Value("${app.ai.model:qwen-turbo}")
    String aiModel;

    @Value("${app.ai.retry.max-attempts:3}")
    int maxRetryAttempts;

    @Value("${app.ai.retry.backoff-ms:800}")
    long retryBackoffMs;

    public AdminController(AuthService authService, MockInterviewService interviewService,
            ResumeHistoryViewRepository resumeHistoryViewRepository, UserRepository userRepository) {
        this.authService = authService;
        this.interviewService = interviewService;
        this.resumeHistoryViewRepository = resumeHistoryViewRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<UserEntity> users = userRepository.findAll();
        List<Map<String, Object>> userViews = new ArrayList<>();
        for (UserEntity user : users) {
            Map<String, Object> userView = new HashMap<>();
            userView.put("id", user.getId());
            userView.put("username", user.getUsername());
            userView.put("email", user.getEmail());
            userView.put("displayName", user.getDisplayName());
            userView.put("role", user.getRole());
            userView.put("status", user.getStatus());
            userView.put("createdAt", user.getCreatedAt());
            userViews.add(userView);
        }
        return ResponseEntity.ok(userViews);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return authService.findUserById(id)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "displayName", user.getDisplayName(),
                        "role", user.getRole(),
                        "status", user.getStatus(),
                        "createdAt", user.getCreatedAt())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            UserEntity user = authService.updateUser(
                    id,
                    request.get("email"),
                    request.get("displayName"));
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "displayName", user.getDisplayName(),
                    "role", user.getRole(),
                    "status", user.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer status = request.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
            }
            authService.updateUserStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "User status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalUsers = userRepository.count();
        long evaluatedCount = resumeHistoryViewRepository.countByStatus("EVALUATED");
        long totalResumes = resumeHistoryViewRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalResumes", totalResumes,
                "totalInterviews", evaluatedCount,
                "activeUsers", totalUsers));
    }

    /**
     * 查看所有用户的简历分析历史
     */
    @GetMapping("/resume-history")
    public ResponseEntity<?> getAllResumeHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(pageable));
    }

    /**
     * 导出简历分析数据
     */
    @GetMapping("/resume-history/export")
    public ResponseEntity<?> exportResumeHistory() {
        return ResponseEntity.ok(Map.of(
                "message", "导出功能开发中"));
    }

    /**
     * 查看所有用户的面试历史
     */
    @GetMapping("/interview-history")
    public ResponseEntity<?> getAllInterviewHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(pageable));
    }

    /**
     * 获取面试模板列表
     */
    @GetMapping("/interview-templates")
    public ResponseEntity<?> getInterviewTemplates() {
        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "name", "Java后端开发面试模板", "description", "包含Java基础、Spring框架、数据库等相关问题"),
                Map.of("id", 2, "name", "前端开发面试模板", "description", "包含HTML/CSS、JavaScript、框架等相关问题"),
                Map.of("id", 3, "name", "算法工程师面试模板", "description", "包含数据结构、算法、系统设计等相关问题")));
    }

    /**
     * 管理角色和权限
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(Map.of(
                "roles", List.of(
                        Map.of("name", "ADMIN", "description", "管理员角色", "permissions", List.of("MANAGE_USERS", "MANAGE_SYSTEM", "VIEW_ALL_DATA")),
                        Map.of("name", "USER", "description", "普通用户角色", "permissions", List.of("VIEW_OWN_DATA", "UPLOAD_RESUME", "TAKE_INTERVIEW")))));
    }

    /**
     * 分配管理员权限
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            if (role == null || (!role.equals("ADMIN") && !role.equals("USER"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }
            authService.updateUserRole(id, role);
            return ResponseEntity.ok(Map.of(
                    "message", "用户角色更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取AI配置
     */
    @GetMapping("/ai-config")
    public ResponseEntity<?> getAiConfig() {
        return ResponseEntity.ok(Map.of(
                "model", aiModel,
                "maxRetryAttempts", maxRetryAttempts,
                "retryBackoffMs", retryBackoffMs));
    }

    /**
     * 更新AI配置
     */
    @PutMapping("/ai-config")
    public ResponseEntity<?> updateAiConfig(@RequestBody Map<String, Object> config) {
        return ResponseEntity.ok(Map.of(
                "message", "AI配置已更新，需要重启应用生效",
                "model", config.getOrDefault("model", aiModel),
                "maxRetryAttempts", config.getOrDefault("maxRetryAttempts", maxRetryAttempts),
                "retryBackoffMs", config.getOrDefault("retryBackoffMs", retryBackoffMs)));
    }

    /**
     * 获取系统限制配置
     */
    @GetMapping("/system-limits")
    public ResponseEntity<?> getSystemLimits() {
        return ResponseEntity.ok(Map.of(
                "maxResumeSize", 10 * 1024 * 1024,
                "maxInterviewQuestions", 20,
                "sessionTimeout", 3600));
    }

    /**
     * 更新系统限制配置
     */
    @PutMapping("/system-limits")
    public ResponseEntity<?> updateSystemLimits(@RequestBody Map<String, Object> limits) {
        return ResponseEntity.ok(Map.of(
                "message", "系统限制配置已更新",
                "maxResumeSize", limits.getOrDefault("maxResumeSize", 10 * 1024 * 1024),
                "maxInterviewQuestions", limits.getOrDefault("maxInterviewQuestions", 20),
                "sessionTimeout", limits.getOrDefault("sessionTimeout", 3600)));
    }

    /**
     * 获取提示词模板列表
     */
    @GetMapping("/prompt-templates")
    public ResponseEntity<?> getPromptTemplates() {
        return ResponseEntity.ok(Map.of(
                "templates", List.of(
                        Map.of("id", 1, "name", "简历分析提示词", "type", "resume-analysis", "description", "用于分析简历的AI提示词模板"),
                        Map.of("id", 2, "name", "面试问题生成", "type", "interview-question", "description", "用于生成面试问题的AI提示词模板"),
                        Map.of("id", 3, "name", "面试评估", "type", "interview-evaluation", "description", "用于评估面试答案的AI提示词模板"))));
    }
}