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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class MockInterviewController {

    private static final Logger logger = LoggerFactory.getLogger(MockInterviewController.class);
    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";
    private static final long SSE_TIMEOUT_MILLIS = 600_000L;

    private final MockInterviewService interviewService;
    private final AuthService authService;
    private final AsyncTaskExecutor sseTaskExecutor;

    public MockInterviewController(
            MockInterviewService interviewService,
            AuthService authService,
            @Qualifier("sseTaskExecutor")
            AsyncTaskExecutor sseTaskExecutor) {
        this.interviewService = interviewService;
        this.authService = authService;
        this.sseTaskExecutor = sseTaskExecutor;
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

    @GetMapping("/interview-entry")
    public String interviewEntryPage() {
        return "interview-entry";
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

            // 读取并返回纯文本（对 PDF 字体扫描异常做一次重试）
            String resumeText = extractResumeText(file, fileName);

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
        } catch (IllegalArgumentException e) {
            logger.warn("简历解析失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
        EmitterState emitterState = createEmitterState();
        SseEmitter emitter = emitterState.emitter;
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            sendEvent(emitterState, "error", Map.of("message", "会话不存在或无权限访问"));
            completeEmitter(emitterState);
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (!sendEvent(emitterState, "progress", Map.of("stage", "start", "message", "正在生成面试问题..."))) {
                    return;
                }
                InterviewQuestions questions = interviewService.generateInterviewQuestions(
                        resumeData.getResumeText(),
                        resumeData.getPositionType()
                );
                if (!sendEvent(emitterState, "progress", Map.of("stage", "saving", "message", "正在保存问题..."))) {
                    return;
                }
                interviewService.saveQuestions(resumeId, questions);
                if (!sendEvent(emitterState, "result", questions)) {
                    return;
                }
                sendEvent(emitterState, "done", Map.of("message", "问题生成完成"));
                completeEmitter(emitterState);
            } catch (Exception e) {
                logger.error("流式生成问题失败", e);
                sendEvent(emitterState, "error", Map.of("message", "生成问题失败：" + e.getMessage()));
                completeEmitterWithError(emitterState, e);
            }
        }, sseTaskExecutor);
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
        EmitterState emitterState = createEmitterState();
        SseEmitter emitter = emitterState.emitter;
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            sendEvent(emitterState, "error", Map.of("message", "会话不存在或无权限访问"));
            completeEmitter(emitterState);
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (!sendEvent(emitterState, "progress", Map.of("stage", "start", "message", "正在评估回答..."))) {
                    return;
                }
                InterviewEvaluation evaluation = interviewService.evaluateAnswers(
                        resumeData.getResumeText(),
                        resumeData.getPositionType(),
                        resumeData.getQuestions(),
                        answers
                );
                if (!sendEvent(emitterState, "progress", Map.of("stage", "saving", "message", "正在保存评估结果..."))) {
                    return;
                }
                interviewService.saveEvaluation(resumeId, evaluation);
                if (!sendEvent(emitterState, "result", evaluation)) {
                    return;
                }
                sendEvent(emitterState, "done", Map.of("message", "评估完成"));
                completeEmitter(emitterState);
            } catch (Exception e) {
                logger.error("流式评估答案失败", e);
                sendEvent(emitterState, "error", Map.of("message", "评估失败：" + e.getMessage()));
                completeEmitterWithError(emitterState, e);
            }
        }, sseTaskExecutor);
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

    private EmitterState createEmitterState() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        EmitterState state = new EmitterState(emitter);
        emitter.onCompletion(() -> state.completed.set(true));
        emitter.onTimeout(() -> {
            if (state.completed.compareAndSet(false, true)) {
                logger.warn("SSE 请求超时: {} ms", SSE_TIMEOUT_MILLIS);
                try {
                    emitter.complete();
                } catch (IllegalStateException ignored) {
                }
            }
        });
        emitter.onError(ex -> state.completed.set(true));
        return state;
    }

    private boolean sendEvent(EmitterState state, String event, Object data) {
        if (state.completed.get()) {
            return false;
        }
        try {
            state.emitter.send(SseEmitter.event().name(event).data(data));
            return true;
        } catch (IOException | IllegalStateException ignored) {
            state.completed.set(true);
            return false;
        }
    }

    private void completeEmitter(EmitterState state) {
        if (state.completed.compareAndSet(false, true)) {
            try {
                state.emitter.complete();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    private void completeEmitterWithError(EmitterState state, Exception ex) {
        if (state.completed.compareAndSet(false, true)) {
            try {
                state.emitter.completeWithError(ex);
            } catch (IllegalStateException ignored) {
            }
        }
    }

    private static class EmitterState {
        private final SseEmitter emitter;
        private final AtomicBoolean completed = new AtomicBoolean(false);

        private EmitterState(SseEmitter emitter) {
            this.emitter = emitter;
        }
    }

    private String extractResumeText(MultipartFile file, String fileName) throws IOException {
        try {
            return readTextByTika(file);
        } catch (RuntimeException ex) {
            if (isPdfFile(fileName) && isPdfFontScanException(ex)) {
                logger.warn("PDF 解析触发字体扫描异常，准备重试一次: {}", ex.getMessage());
                sleepQuietly(300L);
                try {
                    return readTextByTika(file);
                } catch (RuntimeException retryEx) {
                    throw new IllegalArgumentException("PDF 解析失败：检测到系统字体文件异常，请重试或将文件转为 DOCX/TXT 后上传。");
                }
            }
            throw new IllegalArgumentException("文件解析失败：请确认文件未损坏且内容可读取。");
        }
    }

    private String readTextByTika(MultipartFile file) {
        TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
        List<org.springframework.ai.document.Document> docs = reader.read();
        if (docs == null || docs.isEmpty() || docs.get(0) == null) {
            throw new IllegalArgumentException("文件内容为空或无法解析");
        }
        String text = docs.get(0).getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("文件内容为空或无法解析");
        }
        return text;
    }

    private boolean isPdfFile(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    private boolean isPdfFontScanException(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("eof at")
                        || lower.contains("fontbox")
                        || lower.contains("filesystemfontprovider")
                        || lower.contains("ttf")) {
                    return true;
                }
            }
            for (StackTraceElement element : current.getStackTrace()) {
                String className = element.getClassName();
                if (className.contains("org.apache.fontbox.ttf")
                        || className.contains("org.apache.pdfbox.pdmodel.font.FileSystemFontProvider")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
