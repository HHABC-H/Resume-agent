package com.h.resumeagent.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import com.h.resumeagent.service.AIService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class AIServiceImpl implements AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    private final DashScopeChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.retry.max-attempts:3}")
    int aiRetryMaxAttempts = 3;

    @Value("${app.ai.retry.backoff-ms:800}")
    long aiRetryBackoffMs = 800L;

    @Value("${app.ai.retry.max-backoff-ms:4000}")
    long aiRetryMaxBackoffMs = 4_000L;

    public AIServiceImpl(DashScopeChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public String executeAiCallWithRetry(
            String scene,
            Resource systemPromptResource,
            String userPrompt,
            Map<String, Object> variables) {
        try {
            String systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
            String renderedUserPrompt = variables != null && !variables.isEmpty()
                    ? new PromptTemplate(userPrompt).render(variables)
                    : userPrompt;

            return executeAiCallWithRetry(scene, () -> {
                Message systemMessage = new SystemMessage(systemPrompt);
                Message userMessage = new UserMessage(renderedUserPrompt);
                Prompt prompt = new Prompt(
                        java.util.List.of(systemMessage, userMessage),
                        DashScopeChatOptions.builder().temperature(0.7).build()
                );
                return chatModel.call(prompt).getResult().getOutput().getText();
            });
        } catch (IOException e) {
            logger.error("读取系统提示文件失败", e);
            throw new RuntimeException("读取系统提示文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ResumeScoreResult parseResumeScoreResult(String json) throws JsonProcessingException {
        json = cleanJsonResponse(json);
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            Integer overallScore = rootNode.has("overallScore") ? rootNode.get("overallScore").asInt() : 0;
            String summary = rootNode.has("summary") ? rootNode.get("summary").asText() : "";
            ResumeScoreResult.ScoreDetail scoreDetail = new ResumeScoreResult.ScoreDetail();
            if (rootNode.has("scoreDetail")) {
                JsonNode detailNode = rootNode.get("scoreDetail");
                scoreDetail
                        .setProjectScore(detailNode.has("projectScore") ? detailNode.get("projectScore").asInt() : 0);
                scoreDetail.setSkillMatchScore(
                        detailNode.has("skillMatchScore") ? detailNode.get("skillMatchScore").asInt() : 0);
                scoreDetail
                        .setContentScore(detailNode.has("contentScore") ? detailNode.get("contentScore").asInt() : 0);
                scoreDetail.setStructureScore(
                        detailNode.has("structureScore") ? detailNode.get("structureScore").asInt() : 0);
                scoreDetail.setExpressionScore(
                        detailNode.has("expressionScore") ? detailNode.get("expressionScore").asInt() : 0);
            }
            java.util.List<String> strengths = new java.util.ArrayList<>();
            if (rootNode.has("strengths") && rootNode.get("strengths").isArray()) {
                for (JsonNode item : rootNode.get("strengths"))
                    strengths.add(item.asText());
            }
            java.util.List<ResumeScoreResult.Suggestion> suggestions = new java.util.ArrayList<>();
            if (rootNode.has("suggestions") && rootNode.get("suggestions").isArray()) {
                for (JsonNode item : rootNode.get("suggestions")) {
                    ResumeScoreResult.Suggestion suggestion = new ResumeScoreResult.Suggestion();
                    suggestion.setCategory(item.has("category") ? item.get("category").asText() : "");
                    suggestion.setPriority(item.has("priority") ? item.get("priority").asText() : "");
                    suggestion.setIssue(item.has("issue") ? item.get("issue").asText() : "");
                    suggestion.setRecommendation(item.has("recommendation") ? item.get("recommendation").asText() : "");
                    suggestions.add(suggestion);
                }
            }
            return ResumeScoreResult.builder()
                    .overallScore(overallScore).scoreDetail(scoreDetail).summary(summary)
                    .strengths(strengths).suggestions(suggestions).build();
        } catch (Exception e) {
            logger.error("解析简历评分结果失败", e);
            throw new RuntimeException("解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InterviewQuestions parseInterviewQuestions(String json) throws JsonProcessingException {
        json = cleanJsonResponse(json);
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            java.util.List<InterviewQuestions.Question> questions = new java.util.ArrayList<>();
            if (rootNode.has("questions") && rootNode.get("questions").isArray()) {
                for (JsonNode item : rootNode.get("questions")) {
                    InterviewQuestions.Question question = new InterviewQuestions.Question();
                    question.setQuestion(item.has("question") ? item.get("question").asText() : "");
                    question.setType(item.has("type") ? item.get("type").asText() : "");
                    question.setCategory(item.has("category") ? item.get("category").asText() : "");
                    questions.add(question);
                }
            }
            return InterviewQuestions.builder().questions(questions).build();
        } catch (Exception e) {
            logger.error("解析面试问题失败", e);
            throw new RuntimeException("解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String parseFollowUpQuestion(String json) throws JsonProcessingException {
        String cleaned = cleanJsonResponse(StringUtils.defaultString(json));
        if (StringUtils.isBlank(cleaned)) {
            throw new RuntimeException("追问解析失败: 空响应");
        }
        try {
            JsonNode rootNode = objectMapper.readTree(cleaned);
            String followUpQuestion = null;
            if (rootNode.isObject()) {
                followUpQuestion = firstNonBlank(
                        rootNode.path("followUpQuestion").asText(null),
                        rootNode.path("question").asText(null),
                        rootNode.path("followup").asText(null),
                        rootNode.path("nextQuestion").asText(null));
            } else if (rootNode.isTextual()) {
                followUpQuestion = rootNode.asText();
            }
            if (StringUtils.isBlank(followUpQuestion)) {
                throw new RuntimeException("追问解析失败: 缺少 followUpQuestion 字段");
            }
            return followUpQuestion.trim();
        } catch (JsonProcessingException ex) {
            return cleaned.trim();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("追问解析失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public InterviewEvaluation parseInterviewEvaluation(String json) throws JsonProcessingException {
        json = cleanJsonResponse(json);
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            InterviewEvaluation.InterviewEvaluationBuilder builder = InterviewEvaluation.builder();
            builder.sessionId(
                    rootNode.has("sessionId") ? rootNode.get("sessionId").asText() : UUID.randomUUID().toString());
            builder.totalQuestions(rootNode.has("totalQuestions") ? rootNode.get("totalQuestions").asInt() : 0);
            builder.overallScore(rootNode.has("overallScore") ? rootNode.get("overallScore").asInt() : 0);
            builder.overallFeedback(rootNode.has("overallFeedback") ? rootNode.get("overallFeedback").asText() : "");
            java.util.List<InterviewEvaluation.CategoryScore> categoryScores = new java.util.ArrayList<>();
            if (rootNode.has("categoryScores") && rootNode.get("categoryScores").isArray()) {
                for (JsonNode item : rootNode.get("categoryScores")) {
                    InterviewEvaluation.CategoryScore score = new InterviewEvaluation.CategoryScore();
                    score.setCategory(item.has("category") ? item.get("category").asText() : "");
                    score.setScore(item.has("score") ? item.get("score").asInt() : 0);
                    score.setQuestionCount(item.has("questionCount") ? item.get("questionCount").asInt() : 0);
                    categoryScores.add(score);
                }
            }
            builder.categoryScores(categoryScores);
            java.util.List<InterviewEvaluation.QuestionDetail> questionDetails = new java.util.ArrayList<>();
            if (rootNode.has("questionDetails") && rootNode.get("questionDetails").isArray()) {
                for (JsonNode item : rootNode.get("questionDetails")) {
                    InterviewEvaluation.QuestionDetail detail = new InterviewEvaluation.QuestionDetail();
                    detail.setQuestionIndex(item.has("questionIndex") ? item.get("questionIndex").asInt() : 0);
                    detail.setQuestion(item.has("question") ? item.get("question").asText() : "");
                    detail.setCategory(item.has("category") ? item.get("category").asText() : "");
                    detail.setUserAnswer(item.has("userAnswer") ? item.get("userAnswer").asText() : "");
                    detail.setScore(item.has("score") ? item.get("score").asInt() : 0);
                    detail.setFeedback(item.has("feedback") ? item.get("feedback").asText() : "");
                    questionDetails.add(detail);
                }
            }
            builder.questionDetails(questionDetails);
            java.util.List<String> strengths = new java.util.ArrayList<>();
            if (rootNode.has("strengths") && rootNode.get("strengths").isArray()) {
                for (JsonNode item : rootNode.get("strengths"))
                    strengths.add(item.asText());
            }
            builder.strengths(strengths);
            java.util.List<String> improvements = new java.util.ArrayList<>();
            if (rootNode.has("improvements") && rootNode.get("improvements").isArray()) {
                for (JsonNode item : rootNode.get("improvements"))
                    improvements.add(item.asText());
            }
            builder.improvements(improvements);
            java.util.List<InterviewEvaluation.ReferenceAnswer> referenceAnswers = new java.util.ArrayList<>();
            if (rootNode.has("referenceAnswers") && rootNode.get("referenceAnswers").isArray()) {
                for (JsonNode item : rootNode.get("referenceAnswers")) {
                    InterviewEvaluation.ReferenceAnswer answer = new InterviewEvaluation.ReferenceAnswer();
                    answer.setQuestionIndex(item.has("questionIndex") ? item.get("questionIndex").asInt() : 0);
                    answer.setQuestion(item.has("question") ? item.get("question").asText() : "");
                    answer.setReferenceAnswer(item.has("referenceAnswer") ? item.get("referenceAnswer").asText() : "");
                    java.util.List<String> keyPoints = new java.util.ArrayList<>();
                    if (item.has("keyPoints") && item.get("keyPoints").isArray()) {
                        for (JsonNode point : item.get("keyPoints"))
                            keyPoints.add(point.asText());
                    }
                    answer.setKeyPoints(keyPoints);
                    referenceAnswers.add(answer);
                }
            }
            builder.referenceAnswers(referenceAnswers);
            return builder.build();
        } catch (Exception e) {
            logger.error("解析面试评估失败", e);
            throw new RuntimeException("解析失败: " + e.getMessage(), e);
        }
    }

    String executeAiCallWithRetry(String scene, java.util.function.Supplier<String> action) {
        int maxAttempts = Math.max(1, aiRetryMaxAttempts);
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException ex) {
                lastException = ex;
                boolean retryable = isRetryableAiException(ex);
                if (!retryable || attempt == maxAttempts) {
                    throw ex;
                }

                long waitMillis = retryBackoffMillis(attempt);
                logger.warn("{} 第{}次调用失败，将在 {}ms 后重试：{}", scene, attempt, waitMillis, shortMessage(ex));
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("AI调用重试被中断", interruptedException);
                }
            }
        }
        throw lastException == null ? new IllegalStateException(scene + " 调用失败") : lastException;
    }

    private long retryBackoffMillis(int attempt) {
        long base = Math.max(0L, aiRetryBackoffMs);
        long cap = Math.max(base, aiRetryMaxBackoffMs);
        long multiplier = 1L << Math.min(Math.max(0, attempt - 1), 8);
        long delay = base * multiplier;
        return Math.min(delay, cap);
    }

    private boolean isRetryableAiException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ResourceAccessException
                    || current instanceof SocketException
                    || current instanceof SocketTimeoutException
                    || current instanceof HttpServerErrorException
                    || current instanceof HttpClientErrorException.TooManyRequests) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("connection reset")
                        || lower.contains("read timed out")
                        || lower.contains("connect timed out")
                        || lower.contains("timeout")
                        || lower.contains("temporarily unavailable")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private String shortMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (StringUtils.isNotBlank(message)) {
            return message;
        }
        return throwable.getClass().getSimpleName();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String cleanJsonResponse(String json) {
        if (json.startsWith("```json"))
            json = json.substring(7);
        else if (json.startsWith("```"))
            json = json.substring(3);
        if (json.endsWith("```"))
            json = json.substring(0, json.length() - 3);
        return json.trim();
    }
}
