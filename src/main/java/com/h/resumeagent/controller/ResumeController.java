package com.h.resumeagent.controller;

import com.h.resumeagent.interceptor.AuthTokenInterceptor;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import com.h.resumeagent.service.MockInterviewService;
import com.h.resumeagent.utils.ResumeDocumentUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/resume")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);
    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";

    private final MockInterviewService interviewService;
    private final AuthService authService;
    private final ResumeDocumentUtils resumeDocumentUtils;

    public ResumeController(
            MockInterviewService interviewService,
            AuthService authService,
            ResumeDocumentUtils resumeDocumentUtils) {
        this.interviewService = interviewService;
        this.authService = authService;
        this.resumeDocumentUtils = resumeDocumentUtils;
    }

    /**
     * 简历上传页面
     */
    @GetMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadPage() {
        return ResponseEntity.ok(Map.of("message", "Upload page"));
    }

    /**
     * 上传简历并分析
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "positionType", required = false, defaultValue = MockInterviewService.POSITION_BACKEND_JAVA) String positionType,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
            }

            // 检查文件类型
            String fileName = file.getOriginalFilename();
            if (!resumeDocumentUtils.isSupportedResumeFile(fileName)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PDF/DOC/DOCX/TXT is supported"));
            }

            // 提取简历文本
            String resumeText = resumeDocumentUtils.extractResumeText(file);

            // 分析简历
            ResumeScoreResult scoreResult = interviewService.scoreResume(resumeText);
            
            // 保存简历信息
            String resumeId = UUID.randomUUID().toString();
            String normalizedPositionType = interviewService.normalizePositionType(positionType);
            interviewService.saveResume(resumeId, resumeText, scoreResult, userId, normalizedPositionType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("resumeId", resumeId);
            response.put("positionType", normalizedPositionType);
            response.put("positionTypeLabel", interviewService.displayPositionType(normalizedPositionType));
            response.put("scoreResult", scoreResult);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Resume upload failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid resume file: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Scoring failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Scoring failed: " + e.getMessage()));
        }
    }

    /**
     * 简历分析页面
     */
    @GetMapping("/analysis/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> analysisPage(@PathVariable String resumeId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
            "resumeId", resumeId,
            "scoreResult", resumeData.getScoreResult()
        ));
    }

    /**
     * 获取简历分析数据
     */
    @GetMapping("/analysis/{resumeId}/data")
    @ResponseBody
    public ResponseEntity<?> getResumeAnalysis(@PathVariable String resumeId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resumeData.getScoreResult());
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
