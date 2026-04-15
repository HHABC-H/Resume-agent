package com.h.resumeagent.controller;

import com.h.resumeagent.auth.AuthService;
import com.h.resumeagent.auth.AuthTokenInterceptor;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import jakarta.servlet.http.Cookie;
import com.h.resumeagent.service.MockInterviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Controller
public class MockInterviewController {

    private static final Logger logger = LoggerFactory.getLogger(MockInterviewController.class);
    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";

    private final MockInterviewService interviewService;
    private final AuthService authService;

    public MockInterviewController(MockInterviewService interviewService, AuthService authService) {
        this.interviewService = interviewService;
        this.authService = authService;
    }

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    /**
     * 上传简历页面
     */
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    /**
     * 历史记录页面
     */
    @GetMapping("/history")
    public String historyPage() {
        return "history";
    }

    /**
     * 上传简历文件并评分
     */
    @PostMapping("/api/resume/upload")
    @ResponseBody
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "positionType", required = false, defaultValue = MockInterviewService.POSITION_BACKEND_JAVA) String positionType,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "未登录"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
            }


            // 检查文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".pdf") && !fileName.endsWith(".doc") && 
                    !fileName.endsWith(".docx") && !fileName.endsWith(".txt"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "仅支持 PDF、DOC、DOCX 或 TXT 格式的简历文件"));
            }

            // 读取文件内容

            TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
            // 读取并返回纯文本
            String resumeText= reader.read().get(0).getText();

            // 调用 AI 服务进行评分
            ResumeScoreResult scoreResult = interviewService.scoreResume(resumeText);
            
            // 保存简历文本到会话（简化实现，实际应该用 session）
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
            logger.error("上传简历失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "上传失败：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("评分失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "评分失败：" + e.getMessage()));
        }
    }

    /**
     * 简历分析结果页面
     */
    @GetMapping("/analysis/{resumeId}")
    public String analysisPage(@PathVariable String resumeId, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return "redirect:/login";
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return "redirect:/upload";
        }
        
        model.addAttribute("resumeId", resumeId);
        model.addAttribute("scoreResult", resumeData.getScoreResult());
        return "analysis";
    }

    /**
     * 获取简历分析详情
     */
    @GetMapping("/api/resume/{resumeId}/analysis")
    @ResponseBody
    public ResponseEntity<?> getResumeAnalysis(@PathVariable String resumeId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resumeData.getScoreResult());
    }

    /**
     * 获取最近历史记录
     */
    @GetMapping("/api/resume/history")
    @ResponseBody
    public ResponseEntity<?> getHistory(@RequestParam(defaultValue = "20") int limit, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        return ResponseEntity.ok(interviewService.getRecentResumeHistory(userId, limit));
    }

    /**
     * 模拟面试页面
     */
    @GetMapping("/interview/{resumeId}")
    public String interviewPage(@PathVariable String resumeId, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return "redirect:/login";
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return "redirect:/upload";
        }
        
        model.addAttribute("resumeId", resumeId);
        return "interview";
    }

    /**
     * 生成面试问题
     */
    @PostMapping("/api/interview/{resumeId}/questions")
    @ResponseBody
    public ResponseEntity<?> generateQuestions(@PathVariable String resumeId, HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }
            
            InterviewQuestions questions = interviewService.generateInterviewQuestions(
                    resumeData.getResumeText(),
                    resumeData.getPositionType()
            );
            interviewService.saveQuestions(resumeId, questions);
            
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("生成问题失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "生成问题失败：" + e.getMessage()));
        }
    }

    @GetMapping(value = "/api/interview/{resumeId}/questions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter generateQuestionsStream(@PathVariable String resumeId, HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(180000L);
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            sendEvent(emitter, "error", Map.of("message", "会话不存在或无权限访问"));
            emitter.complete();
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                sendEvent(emitter, "progress", Map.of("stage", "start", "message", "正在生成面试问题..."));
                InterviewQuestions questions = interviewService.generateInterviewQuestions(
                        resumeData.getResumeText(),
                        resumeData.getPositionType()
                );
                sendEvent(emitter, "progress", Map.of("stage", "saving", "message", "正在保存问题..."));
                interviewService.saveQuestions(resumeId, questions);
                sendEvent(emitter, "result", questions);
                sendEvent(emitter, "done", Map.of("message", "问题生成完成"));
                emitter.complete();
            } catch (Exception e) {
                logger.error("流式生成问题失败", e);
                sendEvent(emitter, "error", Map.of("message", "生成问题失败：" + e.getMessage()));
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 提交答案并评估
     */
    @PostMapping("/api/interview/{resumeId}/submit")
    @ResponseBody
    public ResponseEntity<?> submitAnswers(
            @PathVariable String resumeId,
            @RequestBody Map<Integer, String> answers,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }
            
            InterviewEvaluation evaluation = interviewService.evaluateAnswers(
                resumeData.getResumeText(), 
                resumeData.getPositionType(),
                resumeData.getQuestions(), 
                answers
            );
            interviewService.saveEvaluation(resumeId, evaluation);
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("评估答案失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "评估失败：" + e.getMessage()));
        }
    }

    @PostMapping(value = "/api/interview/{resumeId}/submit/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter submitAnswersStream(
            @PathVariable String resumeId,
            @RequestBody Map<Integer, String> answers,
            HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(180000L);
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            sendEvent(emitter, "error", Map.of("message", "会话不存在或无权限访问"));
            emitter.complete();
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                sendEvent(emitter, "progress", Map.of("stage", "start", "message", "正在评估回答..."));
                InterviewEvaluation evaluation = interviewService.evaluateAnswers(
                        resumeData.getResumeText(),
                        resumeData.getPositionType(),
                        resumeData.getQuestions(),
                        answers
                );
                sendEvent(emitter, "progress", Map.of("stage", "saving", "message", "正在保存评估结果..."));
                interviewService.saveEvaluation(resumeId, evaluation);
                sendEvent(emitter, "result", evaluation);
                sendEvent(emitter, "done", Map.of("message", "评估完成"));
                emitter.complete();
            } catch (Exception e) {
                logger.error("流式评估答案失败", e);
                sendEvent(emitter, "error", Map.of("message", "评估失败：" + e.getMessage()));
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 查看评估结果页面
     */
    @GetMapping("/result/{resumeId}")
    public String resultPage(@PathVariable String resumeId, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return "redirect:/login";
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return "redirect:/upload";
        }
        
        model.addAttribute("resumeId", resumeId);
        model.addAttribute("evaluation", resumeData.getEvaluation());
        model.addAttribute("questions", resumeData.getQuestions());
        return "result";
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

    private void sendEvent(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException ignored) {
        }
    }
}
