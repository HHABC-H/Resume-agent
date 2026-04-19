package com.h.resumeagent.controller;

import com.h.resumeagent.interceptor.AuthTokenInterceptor;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewFollowUpRequest;
import com.h.resumeagent.common.dto.InterviewFollowUpResponse;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.service.MockInterviewService;
import com.h.resumeagent.utils.SseEmitterUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/interview")
public class InterviewController {

    private static final Logger logger = LoggerFactory.getLogger(InterviewController.class);
    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";

    private final MockInterviewService interviewService;
    private final AuthService authService;
    private final AsyncTaskExecutor sseTaskExecutor;

    public InterviewController(
            MockInterviewService interviewService,
            AuthService authService,
            AsyncTaskExecutor sseTaskExecutor) {
        this.interviewService = interviewService;
        this.authService = authService;
        this.sseTaskExecutor = sseTaskExecutor;
    }

    /**
     * 面试入口页面
     */
    @GetMapping("/entry")
    @ResponseBody
    public ResponseEntity<?> interviewEntryPage() {
        return ResponseEntity.ok(Map.of("message", "Interview entry page"));
    }

    /**
     * 面试页面
     */
    @GetMapping("/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> interviewPage(@PathVariable String resumeId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
            "resumeId", resumeId
        ));
    }

    /**
     * 生成面试问题
     */
    @PostMapping("/{resumeId}/questions")
    @ResponseBody
    public ResponseEntity<?> generateQuestions(@PathVariable String resumeId, @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }

            // 提取题目数量，默认为10
            Integer questionCount = 10;
            if (requestBody.containsKey("questionCount")) {
                Object countObj = requestBody.get("questionCount");
                if (countObj instanceof Number) {
                    questionCount = ((Number) countObj).intValue();
                }
            }

            InterviewQuestions questions = interviewService.generateInterviewQuestions(
                    resumeData.getResumeText(),
                    resumeData.getPositionType(),
                    questionCount);
            interviewService.saveQuestions(resumeId, questions);

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("Generate interview questions failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Generate interview questions failed: " + e.getMessage()));
        }
    }

    /**
     * 生成跟进问题
     */
    @PostMapping("/{resumeId}/follow-up")
    @ResponseBody
    public ResponseEntity<?> generateFollowUpQuestion(
            @PathVariable String resumeId,
            @RequestBody InterviewFollowUpRequest followUpRequest,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }

            if (followUpRequest == null || followUpRequest.getQuestionIndex() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "questionIndex is required"));
            }
            if (StringUtils.isBlank(followUpRequest.getAnswer())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Please answer current question before follow-up"));
            }
            if (resumeData.getQuestions() == null || resumeData.getQuestions().getQuestions() == null
                    || resumeData.getQuestions().getQuestions().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please generate interview questions first"));
            }

            int questionIndex = followUpRequest.getQuestionIndex();
            if (questionIndex < 0 || questionIndex >= resumeData.getQuestions().getQuestions().size()) {
                return ResponseEntity.badRequest().body(Map.of("error", "questionIndex out of range"));
            }

            InterviewQuestions.Question currentQuestion = resumeData.getQuestions().getQuestions().get(questionIndex);
            String followUpQuestion = interviewService.generateFollowUpQuestion(
                    resumeData.getResumeText(),
                    resumeData.getPositionType(),
                    currentQuestion,
                    followUpRequest.getAnswer());

            return ResponseEntity.ok(InterviewFollowUpResponse.builder()
                    .questionIndex(questionIndex)
                    .followUpQuestion(followUpQuestion)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Generate follow-up failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Generate follow-up failed: " + e.getMessage()));
        }
    }

    /**
     * 流式生成面试问题
     */
    @GetMapping(value = "/{resumeId}/questions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter generateQuestionsStream(@PathVariable String resumeId, HttpServletRequest request) {
        SseEmitterUtil.EmitterState emitterState = SseEmitterUtil.createEmitterState();
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            SseEmitterUtil.sendEvent(emitterState, "error", Map.of("message", "Session not found or access denied"));
            SseEmitterUtil.completeEmitter(emitterState);
            return emitterState.emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (!SseEmitterUtil.sendEvent(emitterState, "progress",
                        Map.of("stage", "start", "message", "Generating interview questions..."))) {
                    return;
                }
                InterviewQuestions questions = interviewService.generateInterviewQuestions(
                        resumeData.getResumeText(),
                        resumeData.getPositionType());
                if (!SseEmitterUtil.sendEvent(emitterState, "progress",
                        Map.of("stage", "saving", "message", "Saving questions..."))) {
                    return;
                }
                interviewService.saveQuestions(resumeId, questions);
                if (!SseEmitterUtil.sendEvent(emitterState, "result", questions)) {
                    return;
                }
                SseEmitterUtil.sendEvent(emitterState, "done", Map.of("message", "Interview questions generated"));
                SseEmitterUtil.completeEmitter(emitterState);
            } catch (Exception e) {
                logger.error("Stream generate interview questions failed", e);
                SseEmitterUtil.sendEvent(emitterState, "error",
                        Map.of("message", "Generate interview questions failed: " + e.getMessage()));
                SseEmitterUtil.completeEmitterWithError(emitterState, e);
            }
        }, sseTaskExecutor);
        return emitterState.emitter;
    }

    /**
     * 提交答案并评估
     */
    @PostMapping("/{resumeId}/submit")
    @ResponseBody
    public ResponseEntity<?> submitAnswers(
            @PathVariable String resumeId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }

            // 提取 answers
            Map<Integer, String> answers = new HashMap<>();
            Object answersObj = requestBody.get("answers");
            if (answersObj instanceof Map) {
                ((Map<?, ?>) answersObj).forEach((key, value) -> {
                    try {
                        int index = Integer.parseInt(key.toString());
                        answers.put(index, value != null ? value.toString() : "");
                    } catch (NumberFormatException e) {
                        // 忽略非数字键
                    }
                });
            }

            // 提取 followUpAnswers
            Map<Integer, String> followUpAnswers = new HashMap<>();
            Object followUpAnswersObj = requestBody.get("followUpAnswers");
            if (followUpAnswersObj instanceof Map) {
                ((Map<?, ?>) followUpAnswersObj).forEach((key, value) -> {
                    try {
                        int index = Integer.parseInt(key.toString());
                        followUpAnswers.put(index, value != null ? value.toString() : "");
                    } catch (NumberFormatException e) {
                        // 忽略非数字键
                    }
                });
            }

            InterviewQuestions questions = resumeData.getQuestions();
            if (questions == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No interview questions found"));
            }
            
            InterviewEvaluation evaluation = interviewService.evaluateAnswers(
                    resumeData.getResumeText(),
                    resumeData.getPositionType(),
                    questions,
                    answers,
                    followUpAnswers);
            interviewService.saveEvaluation(resumeId, evaluation);

            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("Evaluate answers failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Evaluate failed: " + e.getMessage()));
        }
    }

    /**
     * 流式评估答案
     */
    @PostMapping(value = "/{resumeId}/submit/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter submitAnswersStream(
            @PathVariable String resumeId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        SseEmitterUtil.EmitterState emitterState = SseEmitterUtil.createEmitterState();
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            SseEmitterUtil.sendEvent(emitterState, "error", Map.of("message", "Session not found or access denied"));
            SseEmitterUtil.completeEmitter(emitterState);
            return emitterState.emitter;
        }

        // 提取 answers
        Map<Integer, String> answers = new HashMap<>();
        Object answersObj = requestBody.get("answers");
        if (answersObj instanceof Map) {
            ((Map<?, ?>) answersObj).forEach((key, value) -> {
                try {
                    int index = Integer.parseInt(key.toString());
                    answers.put(index, value != null ? value.toString() : "");
                } catch (NumberFormatException e) {
                    // 忽略非数字键
                }
            });
        }

        // 提取 followUpAnswers
        Map<Integer, String> followUpAnswers = new HashMap<>();
        Object followUpAnswersObj = requestBody.get("followUpAnswers");
        if (followUpAnswersObj instanceof Map) {
            ((Map<?, ?>) followUpAnswersObj).forEach((key, value) -> {
                try {
                    int index = Integer.parseInt(key.toString());
                    followUpAnswers.put(index, value != null ? value.toString() : "");
                } catch (NumberFormatException e) {
                    // 忽略非数字键
                }
            });
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (!SseEmitterUtil.sendEvent(emitterState, "progress",
                        Map.of("stage", "start", "message", "Evaluating answers..."))) {
                    return;
                }
                InterviewEvaluation evaluation = interviewService.evaluateAnswers(
                        resumeData.getResumeText(),
                        resumeData.getPositionType(),
                        resumeData.getQuestions(),
                        answers,
                        followUpAnswers);
                if (!SseEmitterUtil.sendEvent(emitterState, "progress",
                        Map.of("stage", "saving", "message", "Saving evaluation..."))) {
                    return;
                }
                interviewService.saveEvaluation(resumeId, evaluation);
                if (!SseEmitterUtil.sendEvent(emitterState, "result", evaluation)) {
                    return;
                }
                SseEmitterUtil.sendEvent(emitterState, "done", Map.of("message", "Interview evaluation completed"));
                SseEmitterUtil.completeEmitter(emitterState);
            } catch (Exception e) {
                logger.error("Stream evaluate answers failed", e);
                SseEmitterUtil.sendEvent(emitterState, "error",
                        Map.of("message", "Evaluate failed: " + e.getMessage()));
                SseEmitterUtil.completeEmitterWithError(emitterState, e);
            }
        }, sseTaskExecutor);
        return emitterState.emitter;
    }

    /**
     * 面试结果页面
     */
    @GetMapping("/result/{resumeId}")
    @ResponseBody
    public ResponseEntity<?> resultPage(@PathVariable String resumeId, HttpServletRequest request) {
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
            "evaluation", resumeData.getEvaluation(),
            "questions", resumeData.getQuestions()
        ));
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
