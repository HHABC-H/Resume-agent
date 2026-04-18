package com.h.resumeagent.controller;

import com.h.resumeagent.common.entity.UserEntity;
import com.h.resumeagent.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestAttribute Long currentUserId,
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String displayName = request.get("displayName");
            
            UserEntity user = authService.updateUser(currentUserId, email, displayName);
            
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                    "role", user.getRole()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}