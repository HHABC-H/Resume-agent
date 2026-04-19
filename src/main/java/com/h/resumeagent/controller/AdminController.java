package com.h.resumeagent.controller;

import com.h.resumeagent.common.entity.UserEntity;
import com.h.resumeagent.common.repository.ResumeHistoryViewRepository;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.service.MockInterviewService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final MockInterviewService interviewService;
    private final ResumeHistoryViewRepository resumeHistoryViewRepository;

    public AdminController(AuthService authService, MockInterviewService interviewService, ResumeHistoryViewRepository resumeHistoryViewRepository) {
        this.authService = authService;
        this.interviewService = interviewService;
        this.resumeHistoryViewRepository = resumeHistoryViewRepository;
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        // 这里需要在 UserRepository 中添加 findAll 方法
        // 暂时返回空列表，需要根据实际实现修改
        return ResponseEntity.ok(List.of());
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
                        "createdAt", user.getCreatedAt()
                )))
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
                    request.get("displayName")
            );
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "displayName", user.getDisplayName(),
                    "role", user.getRole(),
                    "status", user.getStatus()
            ));
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
        // 这里需要实现统计功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "totalUsers", 0,
                "totalResumes", 0,
                "totalInterviews", 0,
                "activeUsers", 0
        ));
    }

    /**
     * 查看所有用户的简历分析历史
     */
    @GetMapping("/resume-history")
    public ResponseEntity<?> getAllResumeHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(pageable));
    }

    /**
     * 导出简历分析数据
     */
    @GetMapping("/resume-history/export")
    public ResponseEntity<?> exportResumeHistory() {
        // 这里需要实现导出功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "message", "导出功能开发中"
        ));
    }

    /**
     * 查看所有用户的面试历史
     */
    @GetMapping("/interview-history")
    public ResponseEntity<?> getAllInterviewHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(pageable));
    }

    /**
     * 管理面试模板和问题库
     */
    @GetMapping("/interview-templates")
    public ResponseEntity<?> getInterviewTemplates() {
        // 这里需要实现面试模板管理功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "message", "面试模板管理功能开发中"
        ));
    }

    /**
     * 配置AI模型参数
     */
    @GetMapping("/system-config/ai")
    public ResponseEntity<?> getAIConfig() {
        // 这里需要实现获取AI模型配置的功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "model", "dashscope",
                "apiKey", "sk-669c5a2462c94c689774bb03d465dbcf",
                "retryMaxAttempts", 3,
                "retryBackoffMs", 800
        ));
    }

    /**
     * 更新AI模型参数
     */
    @PutMapping("/system-config/ai")
    public ResponseEntity<?> updateAIConfig(@RequestBody Map<String, Object> config) {
        // 这里需要实现更新AI模型配置的功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "message", "AI模型配置更新成功"
        ));
    }

    /**
     * 管理提示词模板
     */
    @GetMapping("/system-config/prompts")
    public ResponseEntity<?> getPromptTemplates() {
        // 这里需要实现获取提示词模板的功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "message", "提示词模板管理功能开发中"
        ));
    }

    /**
     * 设置系统限制
     */
    @GetMapping("/system-config/limits")
    public ResponseEntity<?> getSystemLimits() {
        // 这里需要实现获取系统限制的功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "resumeUploadSize", "10MB",
                "interviewQuestionCount", 5,
                "maxUsers", 1000
        ));
    }

    /**
     * 更新系统限制
     */
    @PutMapping("/system-config/limits")
    public ResponseEntity<?> updateSystemLimits(@RequestBody Map<String, Object> limits) {
        // 这里需要实现更新系统限制的功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "message", "系统限制更新成功"
        ));
    }

    /**
     * 管理角色和权限
     */
    @GetMapping("/permissions/roles")
    public ResponseEntity<?> getRoles() {
        // 这里需要实现获取角色列表的功能
        // 暂时返回示例数据
        return ResponseEntity.ok(Map.of(
                "roles", List.of(
                        Map.of("name", "ADMIN", "description", "管理员角色"),
                        Map.of("name", "USER", "description", "普通用户角色")
                )
        ));
    }

    /**
     * 分配管理员权限
     */
    @PutMapping("/permissions/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            if (role == null || (!role.equals("ADMIN") && !role.equals("USER"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }
            // 这里需要实现更新用户角色的功能
            // 暂时返回示例数据
            return ResponseEntity.ok(Map.of(
                    "message", "用户角色更新成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 