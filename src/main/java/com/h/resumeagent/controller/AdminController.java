package com.h.resumeagent.controller;

import com.h.resumeagent.common.entity.ResumeHistoryViewEntity;
import com.h.resumeagent.common.entity.ResumeStatus;
import com.h.resumeagent.common.entity.UserEntity;
import com.h.resumeagent.common.repository.ResumeHistoryViewRepository;
import com.h.resumeagent.common.repository.UserRepository;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.service.MockInterviewService;
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
     * 更新用户密码
     */
    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            if (password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "password is required"));
            }
            authService.updateUserPassword(id, password);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
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
        long evaluatedCount = resumeHistoryViewRepository.countByStatus(ResumeStatus.EVALUATED);
        long totalResumes = resumeHistoryViewRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalResumes", totalResumes,
                "totalInterviews", evaluatedCount,
                "activeUsers", totalUsers));
    }

    /**
     * 获取最近活动
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities(@RequestParam(defaultValue = "10") int limit) {
        List<ResumeHistoryViewEntity> recent = resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, limit));
        List<Map<String, Object>> activities = new ArrayList<>();
        for (ResumeHistoryViewEntity item : recent) {
            String icon = switch (item.getStatus()) {
                case ANALYZED -> "📄";
                case QUESTIONS_READY -> "🎯";
                case EVALUATED -> "✅";
                default -> "📋";
            };
            String text;
            if (item.getStatus() == ResumeStatus.EVALUATED) {
                text = String.format("用户%s完成了面试评估", item.getDisplayName() != null ? item.getDisplayName() : item.getUsername());
            } else if (item.getStatus() == ResumeStatus.QUESTIONS_READY) {
                text = String.format("用户%s生成了面试问题", item.getDisplayName() != null ? item.getDisplayName() : item.getUsername());
            } else {
                text = String.format("用户%s上传了简历", item.getDisplayName() != null ? item.getDisplayName() : item.getUsername());
            }
            activities.add(Map.of(
                    "id", activities.size() + 1,
                    "icon", icon,
                    "text", text,
                    "time", item.getUpdatedAt() != null ? item.getUpdatedAt().toString() : item.getCreatedAt().toString()
            ));
        }
        return ResponseEntity.ok(activities);
    }

    /**
     * 查看所有用户的简历分析历史
     */
    @GetMapping("/resume-history")
    public ResponseEntity<?> getAllResumeHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ResumeHistoryViewEntity> list = resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(pageable);
        long total = resumeHistoryViewRepository.count();
        return ResponseEntity.ok(Map.of(
                "content", list,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size),
                "currentPage", page,
                "pageSize", size));
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
}