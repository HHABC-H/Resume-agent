package com.h.resumeagent.controller;

import com.h.resumeagent.interceptor.AuthTokenInterceptor;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.service.MockInterviewService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/history")
public class HistoryController {

    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";

    private final MockInterviewService interviewService;
    private final AuthService authService;

    public HistoryController(
            MockInterviewService interviewService,
            AuthService authService) {
        this.interviewService = interviewService;
        this.authService = authService;
    }

    /**
     * 历史记录页面
     */
    @GetMapping
    public String historyPage() {
        return "history";
    }

    /**
     * 获取历史记录数据
     */
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<?> getHistory(@RequestParam(defaultValue = "20") int limit, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(interviewService.getRecentResumeHistory(userId, limit));
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute(AuthTokenInterceptor.CURRENT_USER_ID_ATTR);
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        String token = resolveToken(request);
        return authService.authenticate(token).map(user -> user.id()).orElse(null);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
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
}
