package com.h.resumeagent.controller;

import com.h.resumeagent.common.entity.UserEntity;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.service.MockInterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final MockInterviewService interviewService;

    public AdminController(AuthService authService, MockInterviewService interviewService) {
        this.authService = authService;
        this.interviewService = interviewService;
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
}