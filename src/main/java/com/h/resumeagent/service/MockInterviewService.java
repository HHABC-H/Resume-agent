package com.h.resumeagent.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeHistoryItem;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import com.h.resumeagent.persistence.entity.EvaluationCategoryScoreEntity;
import com.h.resumeagent.persistence.entity.EvaluationImprovementEntity;
import com.h.resumeagent.persistence.entity.EvaluationQuestionDetailEntity;
import com.h.resumeagent.persistence.entity.EvaluationReferenceAnswerEntity;
import com.h.resumeagent.persistence.entity.EvaluationReferenceKeyPointEntity;
import com.h.resumeagent.persistence.entity.EvaluationStrengthEntity;
import com.h.resumeagent.persistence.entity.InterviewQuestionEntity;
import com.h.resumeagent.persistence.entity.ResumeHistoryViewEntity;
import com.h.resumeagent.persistence.entity.ResumeSessionEntity;
import com.h.resumeagent.persistence.entity.ResumeStatus;
import com.h.resumeagent.persistence.entity.ResumeStrengthEntity;
import com.h.resumeagent.persistence.entity.ResumeSuggestionEntity;
import com.h.resumeagent.persistence.repository.ResumeHistoryViewRepository;
import com.h.resumeagent.persistence.repository.ResumeSessionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MockInterviewService {
    private static final Logger logger = LoggerFactory.getLogger(MockInterviewService.class);
    private static final String STATUS_ANALYZED = "ANALYZED";
    private static final String STATUS_QUESTIONS_READY = "QUESTIONS_READY";
    private static final String STATUS_EVALUATED = "EVALUATED";
    public static final String POSITION_BACKEND_JAVA = "BACKEND_JAVA";
    public static final String POSITION_FRONTEND = "FRONTEND";
    public static final String POSITION_ALGORITHM = "ALGORITHM";

    private final DashScopeChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final Map<String, ResumeData> resumeStorage = new ConcurrentHashMap<>();
    private volatile boolean historyViewAvailable = true;

    @Autowired(required = false)
    private ResumeSessionRepository resumeSessionRepository;

    @Autowired(required = false)
    private ResumeHistoryViewRepository resumeHistoryViewRepository;

    @Value("classpath:/prompt/resume-analysis-system.st")
    Resource resumeAnalysisSystemPromptResource;

    @Value("classpath:/prompt/interview-evaluation-system.st")
    Resource interviewEvaluationSystemPromptresource;

    @Value("classpath:/prompt/interview-question-system.st")
    Resource interviewQuestionsSystemPromptresource;

    @Value("classpath:/prompt/resume-analysis-user.st")
    Resource resumeAnalysisUserresource;

    @Value("${app.ai.retry.max-attempts:3}")
    int aiRetryMaxAttempts = 3;

    @Value("${app.ai.retry.backoff-ms:800}")
    long aiRetryBackoffMs = 800L;

    @Value("${app.ai.retry.max-backoff-ms:4000}")
    long aiRetryMaxBackoffMs = 4_000L;

    public MockInterviewService(DashScopeChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public ResumeScoreResult scoreResume(String resumeText) throws IOException {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(resumeAnalysisSystemPromptResource));
        PromptTemplate promptTemplate = new PromptTemplate(resumeAnalysisUserresource.getContentAsString(StandardCharsets.UTF_8));
        messages.add(new UserMessage(promptTemplate.render(Map.of("resumeText", resumeText))));
        Prompt prompt = new Prompt(messages, DashScopeChatOptions.builder().temperature(0.7).build());
        String response = executeAiCallWithRetry("简历评分", () -> chatModel.call(prompt).getResult().getOutput().getText());
        return parseResumeScoreResult(response);
    }

    public InterviewQuestions generateInterviewQuestions(String resumeText, String positionType) throws JsonProcessingException {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(interviewQuestionsSystemPromptresource));
        String normalizedPositionType = normalizePositionType(positionType);
        String userPrompt = """
                请根据以下简历内容生成面试问题：
                目标岗位：%s
                岗位要求：%s
                ## 候选人简历
                %s
                """.formatted(
                normalizedPositionType,
                buildQuestionPositionContext(normalizedPositionType),
                resumeText
        );
        messages.add(new UserMessage(userPrompt));
        Prompt prompt = new Prompt(messages, DashScopeChatOptions.builder().temperature(0.7).build());
        String response = executeAiCallWithRetry("面试问题生成", () -> chatModel.call(prompt).getResult().getOutput().getText());
        return parseInterviewQuestions(response);
    }

    public InterviewEvaluation evaluateAnswers(
            String resumeText,
            String positionType,
            InterviewQuestions questions,
            Map<Integer, String> answers)
            throws JsonProcessingException {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(interviewEvaluationSystemPromptresource));
        String normalizedPositionType = normalizePositionType(positionType);
        StringBuilder qaText = new StringBuilder();
        for (int i = 0; i < questions.getQuestions().size(); i++) {
            InterviewQuestions.Question q = questions.getQuestions().get(i);
            String answer = StringUtils.isBlank(answers.get(i)) ? "未作答" : answers.get(i);
            qaText.append("问题 %d [%s]: %s%n".formatted(i + 1, q.getType(), q.getQuestion()));
            qaText.append("候选人回答：%s%n%n".formatted(answer));
        }
        messages.add(new UserMessage("""
                请评估以下面试问答：
                目标岗位：%s
                评分侧重点：%s
                %s
                """.formatted(
                normalizedPositionType,
                buildEvaluationPositionContext(normalizedPositionType),
                qaText
        )));
        Prompt prompt = new Prompt(messages, DashScopeChatOptions.builder().temperature(0.7).build());
        String response = executeAiCallWithRetry("面试答案评估", () -> chatModel.call(prompt).getResult().getOutput().getText());
        return parseInterviewEvaluation(response);
    }

    public void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult) {
        saveResume(resumeId, resumeText, scoreResult, null, POSITION_BACKEND_JAVA);
    }

    public void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult, Long userId) {
        saveResume(resumeId, resumeText, scoreResult, userId, POSITION_BACKEND_JAVA);
    }

    @Transactional
    public void saveResume(
            String resumeId,
            String resumeText,
            ResumeScoreResult scoreResult,
            Long userId,
            String positionType) {
        String normalizedPositionType = normalizePositionType(positionType);
        if (!isPersistenceEnabled()) {
            LocalDateTime now = LocalDateTime.now();
            resumeStorage.put(resumeId, ResumeData.builder()
                    .resumeId(resumeId).resumeText(resumeText).positionType(normalizedPositionType).scoreResult(scoreResult)
                    .status(STATUS_ANALYZED).createdAt(now).updatedAt(now).build());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ResumeSessionEntity session = resumeSessionRepository.findByResumeId(resumeId).orElseGet(ResumeSessionEntity::new);
        ensureCollections(session);
        if (session.getCreatedAt() == null) {
            session.setCreatedAt(now);
        }
        if (userId != null) {
            session.setUserId(userId);
        }

        session.setResumeId(resumeId);
        session.setResumeText(resumeText);
        session.setStatus(ResumeStatus.ANALYZED);
        session.setPositionType(normalizedPositionType);
        session.setResumeOverallScore(scoreResult == null ? null : scoreResult.getOverallScore());
        session.setResumeSummary(scoreResult == null ? null : scoreResult.getSummary());
        ResumeScoreResult.ScoreDetail d = scoreResult == null ? null : scoreResult.getScoreDetail();
        session.setScoreProject(d == null ? null : d.getProjectScore());
        session.setScoreSkillMatch(d == null ? null : d.getSkillMatchScore());
        session.setScoreContent(d == null ? null : d.getContentScore());
        session.setScoreStructure(d == null ? null : d.getStructureScore());
        session.setScoreExpression(d == null ? null : d.getExpressionScore());
        session.setEvaluationSessionId(null);
        session.setEvaluationTotalQuestions(null);
        session.setEvaluationOverallScore(null);
        session.setEvaluationOverallFeedback(null);
        session.setUpdatedAt(now);

        replaceResumeStrengths(session, scoreResult == null ? List.of() : scoreResult.getStrengths());
        replaceResumeSuggestions(session, scoreResult == null ? List.of() : scoreResult.getSuggestions());
        replaceInterviewQuestions(session, List.of());
        session.getEvaluationCategoryScores().clear();
        session.getEvaluationQuestionDetails().clear();
        session.getEvaluationStrengths().clear();
        session.getEvaluationImprovements().clear();
        session.getEvaluationReferenceAnswers().clear();
        resumeSessionRepository.save(session);
    }

    @Transactional
    public void saveQuestions(String resumeId, InterviewQuestions questions) {
        if (!isPersistenceEnabled()) {
            ResumeData resumeData = resumeStorage.get(resumeId);
            if (resumeData != null) {
                resumeData.setQuestions(questions);
                resumeData.setStatus(STATUS_QUESTIONS_READY);
                resumeData.setUpdatedAt(LocalDateTime.now());
                resumeStorage.put(resumeId, resumeData);
            }
            return;
        }
        resumeSessionRepository.findByResumeId(resumeId).ifPresent(session -> {
            ensureCollections(session);
            replaceInterviewQuestions(session, questions == null ? List.of() : questions.getQuestions());
            session.setStatus(ResumeStatus.QUESTIONS_READY);
            session.setUpdatedAt(LocalDateTime.now());
            resumeSessionRepository.save(session);
        });
    }

    @Transactional
    public void saveEvaluation(String resumeId, InterviewEvaluation evaluation) {
        if (!isPersistenceEnabled()) {
            ResumeData resumeData = resumeStorage.get(resumeId);
            if (resumeData != null) {
                resumeData.setEvaluation(evaluation);
                resumeData.setStatus(STATUS_EVALUATED);
                resumeData.setUpdatedAt(LocalDateTime.now());
                resumeStorage.put(resumeId, resumeData);
            }
            return;
        }
        resumeSessionRepository.findByResumeId(resumeId).ifPresent(session -> {
            ensureCollections(session);
            session.setEvaluationSessionId(evaluation == null ? null : evaluation.getSessionId());
            session.setEvaluationTotalQuestions(evaluation == null ? null : evaluation.getTotalQuestions());
            session.setEvaluationOverallScore(evaluation == null ? null : evaluation.getOverallScore());
            session.setEvaluationOverallFeedback(evaluation == null ? null : evaluation.getOverallFeedback());
            replaceEvalCategory(session, evaluation == null ? List.of() : evaluation.getCategoryScores());
            replaceEvalDetail(session, evaluation == null ? List.of() : evaluation.getQuestionDetails());
            replaceEvalStrength(session, evaluation == null ? List.of() : evaluation.getStrengths());
            replaceEvalImprovement(session, evaluation == null ? List.of() : evaluation.getImprovements());
            replaceEvalReference(session, evaluation == null ? List.of() : evaluation.getReferenceAnswers());
            session.setStatus(ResumeStatus.EVALUATED);
            session.setUpdatedAt(LocalDateTime.now());
            resumeSessionRepository.save(session);
        });
    }

    @Transactional(readOnly = true)
    public ResumeData getResumeById(String resumeId) {
        return getResumeById(resumeId, null);
    }

    @Transactional(readOnly = true)
    public ResumeData getResumeById(String resumeId, Long userId) {
        if (!isPersistenceEnabled()) {
            return resumeStorage.get(resumeId);
        }
        return resumeSessionRepository.findByResumeId(resumeId)
                .filter(session -> userId == null || Objects.equals(session.getUserId(), userId))
                .map(this::toResumeData)
                .orElse(null);
    }

    public List<ResumeHistoryItem> getRecentResumeHistory(int limit) {
        return getRecentResumeHistory(null, limit);
    }

    @Transactional(readOnly = true)
    public List<ResumeHistoryItem> getRecentResumeHistory(Long userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        if (!isPersistenceEnabled()) {
            return resumeStorage.values().stream()
                    .sorted(Comparator.comparing(ResumeData::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(safeLimit)
                    .map(this::toHistoryItem)
                    .collect(Collectors.toList());
        }
        PageRequest page = PageRequest.of(0, safeLimit);

        if (historyViewAvailable) {
            try {
                List<ResumeHistoryViewEntity> rows = userId == null
                        ? resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(page)
                        : resumeHistoryViewRepository.findByUserIdOrderByUpdatedAtDesc(userId, page);
                return rows.stream().map(this::toHistoryItem).collect(Collectors.toList());
            } catch (RuntimeException ex) {
                if (isHistoryViewSchemaException(ex)) {
                    historyViewAvailable = false;
                    logger.warn("历史视图查询失败，已降级为 resume_session 查询: {}", shortMessage(ex));
                } else {
                    throw ex;
                }
            }
        }

        List<ResumeSessionEntity> sessions = userId == null
                ? resumeSessionRepository.findAllByOrderByUpdatedAtDesc(page)
                : resumeSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, page);
        return sessions.stream().map(this::toHistoryItem).collect(Collectors.toList());
    }

    private boolean isPersistenceEnabled() {
        return resumeSessionRepository != null && resumeHistoryViewRepository != null;
    }

    private void ensureCollections(ResumeSessionEntity s) {
        if (s.getResumeStrengths() == null) s.setResumeStrengths(new ArrayList<>());
        if (s.getResumeSuggestions() == null) s.setResumeSuggestions(new ArrayList<>());
        if (s.getInterviewQuestions() == null) s.setInterviewQuestions(new ArrayList<>());
        if (s.getEvaluationCategoryScores() == null) s.setEvaluationCategoryScores(new ArrayList<>());
        if (s.getEvaluationQuestionDetails() == null) s.setEvaluationQuestionDetails(new ArrayList<>());
        if (s.getEvaluationStrengths() == null) s.setEvaluationStrengths(new ArrayList<>());
        if (s.getEvaluationImprovements() == null) s.setEvaluationImprovements(new ArrayList<>());
        if (s.getEvaluationReferenceAnswers() == null) s.setEvaluationReferenceAnswers(new ArrayList<>());
    }

    private void replaceResumeStrengths(ResumeSessionEntity s, List<String> strengths) {
        s.getResumeStrengths().clear();
        if (strengths == null) return;
        for (int i = 0; i < strengths.size(); i++) {
            ResumeStrengthEntity e = new ResumeStrengthEntity();
            e.setResumeSession(s);
            e.setStrengthText(strengths.get(i));
            e.setSortOrder(i);
            s.getResumeStrengths().add(e);
        }
    }

    private void replaceResumeSuggestions(ResumeSessionEntity s, List<ResumeScoreResult.Suggestion> suggestions) {
        s.getResumeSuggestions().clear();
        if (suggestions == null) return;
        for (int i = 0; i < suggestions.size(); i++) {
            ResumeScoreResult.Suggestion it = suggestions.get(i);
            ResumeSuggestionEntity e = new ResumeSuggestionEntity();
            e.setResumeSession(s);
            e.setCategory(it == null ? null : it.getCategory());
            e.setPriorityLevel(it == null ? null : it.getPriority());
            e.setIssueText(it == null ? null : it.getIssue());
            e.setRecommendation(it == null ? null : it.getRecommendation());
            e.setSortOrder(i);
            s.getResumeSuggestions().add(e);
        }
    }

    private void replaceInterviewQuestions(ResumeSessionEntity s, List<InterviewQuestions.Question> questions) {
        s.getInterviewQuestions().clear();
        if (questions == null) return;
        for (int i = 0; i < questions.size(); i++) {
            InterviewQuestions.Question it = questions.get(i);
            InterviewQuestionEntity e = new InterviewQuestionEntity();
            e.setResumeSession(s);
            e.setQuestionText(it == null ? null : it.getQuestion());
            e.setQuestionType(it == null ? null : it.getType());
            e.setCategory(it == null ? null : it.getCategory());
            e.setSortOrder(i);
            s.getInterviewQuestions().add(e);
        }
    }

    private void replaceEvalCategory(ResumeSessionEntity s, List<InterviewEvaluation.CategoryScore> list) {
        s.getEvaluationCategoryScores().clear();
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            InterviewEvaluation.CategoryScore it = list.get(i);
            EvaluationCategoryScoreEntity e = new EvaluationCategoryScoreEntity();
            e.setResumeSession(s);
            e.setCategory(it == null ? null : it.getCategory());
            e.setScore(it == null ? null : it.getScore());
            e.setQuestionCount(it == null ? null : it.getQuestionCount());
            e.setSortOrder(i);
            s.getEvaluationCategoryScores().add(e);
        }
    }

    private void replaceEvalDetail(ResumeSessionEntity s, List<InterviewEvaluation.QuestionDetail> list) {
        s.getEvaluationQuestionDetails().clear();
        if (list == null) return;
        for (InterviewEvaluation.QuestionDetail it : list) {
            EvaluationQuestionDetailEntity e = new EvaluationQuestionDetailEntity();
            e.setResumeSession(s);
            e.setQuestionIndex(it == null ? null : it.getQuestionIndex());
            e.setQuestionText(it == null ? null : it.getQuestion());
            e.setCategory(it == null ? null : it.getCategory());
            e.setUserAnswer(it == null ? null : it.getUserAnswer());
            e.setScore(it == null ? null : it.getScore());
            e.setFeedback(it == null ? null : it.getFeedback());
            s.getEvaluationQuestionDetails().add(e);
        }
    }

    private void replaceEvalStrength(ResumeSessionEntity s, List<String> list) {
        s.getEvaluationStrengths().clear();
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            EvaluationStrengthEntity e = new EvaluationStrengthEntity();
            e.setResumeSession(s);
            e.setStrengthText(list.get(i));
            e.setSortOrder(i);
            s.getEvaluationStrengths().add(e);
        }
    }

    private void replaceEvalImprovement(ResumeSessionEntity s, List<String> list) {
        s.getEvaluationImprovements().clear();
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            EvaluationImprovementEntity e = new EvaluationImprovementEntity();
            e.setResumeSession(s);
            e.setImprovementText(list.get(i));
            e.setSortOrder(i);
            s.getEvaluationImprovements().add(e);
        }
    }

    private void replaceEvalReference(ResumeSessionEntity s, List<InterviewEvaluation.ReferenceAnswer> list) {
        s.getEvaluationReferenceAnswers().clear();
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            InterviewEvaluation.ReferenceAnswer it = list.get(i);
            EvaluationReferenceAnswerEntity e = new EvaluationReferenceAnswerEntity();
            e.setResumeSession(s);
            e.setQuestionIndex(it == null ? null : it.getQuestionIndex());
            e.setQuestionText(it == null ? null : it.getQuestion());
            e.setReferenceAnswer(it == null ? null : it.getReferenceAnswer());
            e.setSortOrder(i);
            List<EvaluationReferenceKeyPointEntity> points = new ArrayList<>();
            List<String> keys = it == null ? List.of() : Optional.ofNullable(it.getKeyPoints()).orElse(List.of());
            for (int j = 0; j < keys.size(); j++) {
                EvaluationReferenceKeyPointEntity p = new EvaluationReferenceKeyPointEntity();
                p.setReferenceAnswer(e);
                p.setKeyPoint(keys.get(j));
                p.setSortOrder(j);
                points.add(p);
            }
            e.setKeyPoints(points);
            s.getEvaluationReferenceAnswers().add(e);
        }
    }

    private ResumeData toResumeData(ResumeSessionEntity s) {
        ResumeScoreResult score = ResumeScoreResult.builder()
                .overallScore(s.getResumeOverallScore())
                .scoreDetail(new ResumeScoreResult.ScoreDetail(
                        s.getScoreProject(), s.getScoreSkillMatch(), s.getScoreContent(), s.getScoreStructure(), s.getScoreExpression()))
                .summary(s.getResumeSummary())
                .strengths(Optional.ofNullable(s.getResumeStrengths()).orElse(List.of()).stream()
                        .sorted(Comparator.comparing(ResumeStrengthEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                        .map(ResumeStrengthEntity::getStrengthText).collect(Collectors.toList()))
                .suggestions(Optional.ofNullable(s.getResumeSuggestions()).orElse(List.of()).stream()
                        .sorted(Comparator.comparing(ResumeSuggestionEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                        .map(it -> new ResumeScoreResult.Suggestion(it.getCategory(), it.getPriorityLevel(), it.getIssueText(), it.getRecommendation()))
                        .collect(Collectors.toList()))
                .build();

        InterviewQuestions questions = null;
        if (!Optional.ofNullable(s.getInterviewQuestions()).orElse(List.of()).isEmpty()) {
            questions = InterviewQuestions.builder()
                    .questions(s.getInterviewQuestions().stream()
                            .sorted(Comparator.comparing(InterviewQuestionEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                            .map(it -> new InterviewQuestions.Question(it.getQuestionText(), it.getQuestionType(), it.getCategory()))
                            .collect(Collectors.toList()))
                    .build();
        }

        InterviewEvaluation eval = null;
        if (s.getEvaluationOverallScore() != null || !Optional.ofNullable(s.getEvaluationQuestionDetails()).orElse(List.of()).isEmpty()) {
            eval = InterviewEvaluation.builder()
                    .sessionId(s.getEvaluationSessionId())
                    .totalQuestions(s.getEvaluationTotalQuestions())
                    .overallScore(s.getEvaluationOverallScore())
                    .overallFeedback(s.getEvaluationOverallFeedback())
                    .categoryScores(Optional.ofNullable(s.getEvaluationCategoryScores()).orElse(List.of()).stream()
                            .sorted(Comparator.comparing(EvaluationCategoryScoreEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                            .map(it -> new InterviewEvaluation.CategoryScore(it.getCategory(), it.getScore(), it.getQuestionCount()))
                            .collect(Collectors.toList()))
                    .questionDetails(Optional.ofNullable(s.getEvaluationQuestionDetails()).orElse(List.of()).stream()
                            .sorted(Comparator.comparing(EvaluationQuestionDetailEntity::getQuestionIndex, Comparator.nullsLast(Integer::compareTo)))
                            .map(it -> new InterviewEvaluation.QuestionDetail(it.getQuestionIndex(), it.getQuestionText(), it.getCategory(), it.getUserAnswer(), it.getScore(), it.getFeedback()))
                            .collect(Collectors.toList()))
                    .strengths(Optional.ofNullable(s.getEvaluationStrengths()).orElse(List.of()).stream()
                            .sorted(Comparator.comparing(EvaluationStrengthEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                            .map(EvaluationStrengthEntity::getStrengthText).collect(Collectors.toList()))
                    .improvements(Optional.ofNullable(s.getEvaluationImprovements()).orElse(List.of()).stream()
                            .sorted(Comparator.comparing(EvaluationImprovementEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                            .map(EvaluationImprovementEntity::getImprovementText).collect(Collectors.toList()))
                    .referenceAnswers(Optional.ofNullable(s.getEvaluationReferenceAnswers()).orElse(List.of()).stream()
                            .sorted(Comparator.comparing(EvaluationReferenceAnswerEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                            .map(it -> new InterviewEvaluation.ReferenceAnswer(
                                    it.getQuestionIndex(), it.getQuestionText(), it.getReferenceAnswer(),
                                    Optional.ofNullable(it.getKeyPoints()).orElse(List.of()).stream()
                                            .sorted(Comparator.comparing(EvaluationReferenceKeyPointEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                                            .map(EvaluationReferenceKeyPointEntity::getKeyPoint).collect(Collectors.toList())))
                            .collect(Collectors.toList()))
                    .build();
        }

        return ResumeData.builder()
                .resumeId(s.getResumeId())
                .resumeText(s.getResumeText())
                .positionType(normalizePositionType(s.getPositionType()))
                .scoreResult(score)
                .questions(questions)
                .evaluation(eval)
                .status(s.getStatus() == null ? STATUS_ANALYZED : s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private ResumeHistoryItem toHistoryItem(ResumeData d) {
        int questionCount = d.getQuestions() == null || d.getQuestions().getQuestions() == null ? 0 : d.getQuestions().getQuestions().size();
        Integer evalScore = d.getEvaluation() == null ? null : d.getEvaluation().getOverallScore();
        Integer resumeScore = d.getScoreResult() == null ? null : d.getScoreResult().getOverallScore();
        return ResumeHistoryItem.builder()
                .resumeId(d.getResumeId()).status(StringUtils.defaultIfBlank(d.getStatus(), STATUS_ANALYZED))
                .positionType(normalizePositionType(d.getPositionType()))
                .resumeScore(resumeScore).evaluationScore(evalScore).questionCount(questionCount)
                .createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
    }

    private ResumeHistoryItem toHistoryItem(ResumeHistoryViewEntity v) {
        return ResumeHistoryItem.builder()
                .resumeId(v.getResumeId())
                .status(v.getStatus() == null ? STATUS_ANALYZED : v.getStatus().name())
                .positionType(normalizePositionType(v.getPositionType()))
                .resumeScore(v.getResumeOverallScore())
                .evaluationScore(v.getEvaluationOverallScore())
                .questionCount(v.getQuestionCount() == null ? 0 : v.getQuestionCount().intValue())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    private ResumeHistoryItem toHistoryItem(ResumeSessionEntity s) {
        int questionCount = Optional.ofNullable(s.getInterviewQuestions()).orElse(List.of()).size();
        return ResumeHistoryItem.builder()
                .resumeId(s.getResumeId())
                .status(s.getStatus() == null ? STATUS_ANALYZED : s.getStatus().name())
                .positionType(normalizePositionType(s.getPositionType()))
                .resumeScore(s.getResumeOverallScore())
                .evaluationScore(s.getEvaluationOverallScore())
                .questionCount(questionCount)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    public String normalizePositionType(String positionType) {
        if (StringUtils.isBlank(positionType)) {
            return POSITION_BACKEND_JAVA;
        }
        String normalized = positionType.trim().toUpperCase();
        if (POSITION_FRONTEND.equals(normalized) || POSITION_ALGORITHM.equals(normalized)) {
            return normalized;
        }
        return POSITION_BACKEND_JAVA;
    }

    public String displayPositionType(String positionType) {
        String normalized = normalizePositionType(positionType);
        return switch (normalized) {
            case POSITION_FRONTEND -> "前端";
            case POSITION_ALGORITHM -> "算法";
            default -> "后端Java";
        };
    }

    private String buildQuestionPositionContext(String normalizedPositionType) {
        return switch (normalizedPositionType) {
            case POSITION_FRONTEND ->
                    "聚焦 HTML/CSS/JavaScript/TypeScript、Vue/React、浏览器渲染机制、性能优化、前端工程化与调试。";
            case POSITION_ALGORITHM ->
                    "聚焦数据结构、算法设计、复杂度分析、边界条件处理、代码正确性与优化思路。";
            default ->
                    "聚焦 Java 后端基础、并发、JVM、数据库、缓存、Spring 生态、系统设计与性能优化。";
        };
    }

    private String buildEvaluationPositionContext(String normalizedPositionType) {
        return switch (normalizedPositionType) {
            case POSITION_FRONTEND ->
                    "重点看前端技术深度、工程化实践、性能与兼容性处理能力、问题定位与用户体验意识。";
            case POSITION_ALGORITHM ->
                    "重点看建模能力、算法正确性、复杂度控制、边界分析、表达清晰度和可实现性。";
            default ->
                    "重点看后端技术深度、系统设计思维、性能与稳定性意识、工程实践与表达完整性。";
        };
    }

    private ResumeScoreResult parseResumeScoreResult(String json) throws JsonProcessingException {
        json = cleanJsonResponse(json);
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            Integer overallScore = rootNode.has("overallScore") ? rootNode.get("overallScore").asInt() : 0;
            String summary = rootNode.has("summary") ? rootNode.get("summary").asText() : "";
            ResumeScoreResult.ScoreDetail scoreDetail = new ResumeScoreResult.ScoreDetail();
            if (rootNode.has("scoreDetail")) {
                JsonNode detailNode = rootNode.get("scoreDetail");
                scoreDetail.setProjectScore(detailNode.has("projectScore") ? detailNode.get("projectScore").asInt() : 0);
                scoreDetail.setSkillMatchScore(detailNode.has("skillMatchScore") ? detailNode.get("skillMatchScore").asInt() : 0);
                scoreDetail.setContentScore(detailNode.has("contentScore") ? detailNode.get("contentScore").asInt() : 0);
                scoreDetail.setStructureScore(detailNode.has("structureScore") ? detailNode.get("structureScore").asInt() : 0);
                scoreDetail.setExpressionScore(detailNode.has("expressionScore") ? detailNode.get("expressionScore").asInt() : 0);
            }
            List<String> strengths = new ArrayList<>();
            if (rootNode.has("strengths") && rootNode.get("strengths").isArray()) {
                for (JsonNode item : rootNode.get("strengths")) strengths.add(item.asText());
            }
            List<ResumeScoreResult.Suggestion> suggestions = new ArrayList<>();
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

    private InterviewQuestions parseInterviewQuestions(String json) throws JsonProcessingException {
        json = cleanJsonResponse(json);
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            List<InterviewQuestions.Question> questions = new ArrayList<>();
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

    private InterviewEvaluation parseInterviewEvaluation(String json) throws JsonProcessingException {
        json = cleanJsonResponse(json);
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            InterviewEvaluation.InterviewEvaluationBuilder builder = InterviewEvaluation.builder();
            builder.sessionId(rootNode.has("sessionId") ? rootNode.get("sessionId").asText() : UUID.randomUUID().toString());
            builder.totalQuestions(rootNode.has("totalQuestions") ? rootNode.get("totalQuestions").asInt() : 0);
            builder.overallScore(rootNode.has("overallScore") ? rootNode.get("overallScore").asInt() : 0);
            builder.overallFeedback(rootNode.has("overallFeedback") ? rootNode.get("overallFeedback").asText() : "");
            List<InterviewEvaluation.CategoryScore> categoryScores = new ArrayList<>();
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
            List<InterviewEvaluation.QuestionDetail> questionDetails = new ArrayList<>();
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
            List<String> strengths = new ArrayList<>();
            if (rootNode.has("strengths") && rootNode.get("strengths").isArray()) {
                for (JsonNode item : rootNode.get("strengths")) strengths.add(item.asText());
            }
            builder.strengths(strengths);
            List<String> improvements = new ArrayList<>();
            if (rootNode.has("improvements") && rootNode.get("improvements").isArray()) {
                for (JsonNode item : rootNode.get("improvements")) improvements.add(item.asText());
            }
            builder.improvements(improvements);
            List<InterviewEvaluation.ReferenceAnswer> referenceAnswers = new ArrayList<>();
            if (rootNode.has("referenceAnswers") && rootNode.get("referenceAnswers").isArray()) {
                for (JsonNode item : rootNode.get("referenceAnswers")) {
                    InterviewEvaluation.ReferenceAnswer answer = new InterviewEvaluation.ReferenceAnswer();
                    answer.setQuestionIndex(item.has("questionIndex") ? item.get("questionIndex").asInt() : 0);
                    answer.setQuestion(item.has("question") ? item.get("question").asText() : "");
                    answer.setReferenceAnswer(item.has("referenceAnswer") ? item.get("referenceAnswer").asText() : "");
                    List<String> keyPoints = new ArrayList<>();
                    if (item.has("keyPoints") && item.get("keyPoints").isArray()) {
                        for (JsonNode point : item.get("keyPoints")) keyPoints.add(point.asText());
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

    private String cleanJsonResponse(String json) {
        if (json.startsWith("```json")) json = json.substring(7);
        else if (json.startsWith("```")) json = json.substring(3);
        if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
        return json.trim();
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

    private boolean isHistoryViewSchemaException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DataAccessException) {
                String message = current.getMessage();
                if (message != null) {
                    String lower = message.toLowerCase();
                    if (lower.contains("unknown column")
                            && lower.contains("position_type")) {
                        return true;
                    }
                }
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("unknown column")
                        && lower.contains("position_type")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
