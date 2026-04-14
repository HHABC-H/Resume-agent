package com.h.resumeagent.controller;

import com.h.resumeagent.auth.AuthService;
import com.h.resumeagent.persistence.entity.UserEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            UserEntity user = authService.register(
                    request.username(),
                    request.email(),
                    request.password(),
                    request.displayName()
            );
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", StringUtils.defaultString(user.getEmail()),
                    "displayName", StringUtils.defaultString(user.getDisplayName())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthService.LoginResult result = authService.login(request.username(), request.password());
            return ResponseEntity.ok(Map.of(
                    "token", result.token(),
                    "expiresAt", result.expiresAt(),
                    "userId", result.user().id(),
                    "username", result.user().username()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        String token = resolveToken(authorization, request);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "已退出登录"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        String token = resolveToken(authorization, request);
        Long userId = authService.authenticate(token).map(user -> user.id()).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        return authService.findUserById(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", StringUtils.defaultString(user.getEmail()),
                        "displayName", StringUtils.defaultString(user.getDisplayName())
                )))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "用户不存在")));
    }

    private String extractBearerToken(String authHeader) {
        if (StringUtils.isBlank(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            return null;
        }
        return StringUtils.trim(authHeader.substring("Bearer ".length()));
    }

    private String resolveToken(String authHeader, HttpServletRequest request) {
        String token = extractBearerToken(authHeader);
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public record RegisterRequest(String username, String email, String password, String displayName) {
    }

    public record LoginRequest(String username, String password) {
    }
}
