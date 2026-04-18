package com.h.resumeagent.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.h.resumeagent.common.dto.AuthenticatedUser;
import com.h.resumeagent.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER_ID_ATTR = "CURRENT_USER_ID";
    public static final String CURRENT_USERNAME_ATTR = "CURRENT_USERNAME";
    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthTokenInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = resolveToken(request);
        if (StringUtils.isBlank(token)) {
            unauthorized(response, "未登录，请先登录");
            return false;
        }

        return authService.authenticate(token).map(user -> {
            request.setAttribute(CURRENT_USER_ID_ATTR, user.id());
            request.setAttribute(CURRENT_USERNAME_ATTR, user.username());
            return true;
        }).orElseGet(() -> {
            unauthorized(response, "登录状态已失效，请重新登录");
            return false;
        });
    }

    private String extractBearerToken(String authHeader) {
        if (StringUtils.isBlank(authHeader)) {
            return null;
        }
        if (!StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            return null;
        }
        return StringUtils.trim(authHeader.substring("Bearer ".length()));
    }

    private String resolveToken(HttpServletRequest request) {
        String token = extractBearerToken(request.getHeader("Authorization"));
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

    private void unauthorized(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
        } catch (Exception ignored) {
        }
    }
}