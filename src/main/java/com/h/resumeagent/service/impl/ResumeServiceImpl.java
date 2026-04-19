package com.h.resumeagent.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import com.h.resumeagent.common.entity.ResumeSessionEntity;
import com.h.resumeagent.common.entity.ResumeStatus;
import com.h.resumeagent.common.entity.ResumeStrengthEntity;
import com.h.resumeagent.common.entity.ResumeSuggestionEntity;
import com.h.resumeagent.common.repository.ResumeSessionRepository;
import com.h.resumeagent.service.PositionService;
import com.h.resumeagent.service.ResumeService;
import com.h.resumeagent.service.AIService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private static final String STATUS_ANALYZED = "ANALYZED";

    private final AIService aiService;
    private final PositionService positionService;
    private final ObjectMapper objectMapper;
    private final Map<String, ResumeData> resumeStorage = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private ResumeSessionRepository resumeSessionRepository;

    @Value("classpath:/prompt/resume-analysis-system.st")
    Resource resumeAnalysisSystemPromptResource;

    @Value("classpath:/prompt/resume-analysis-user.st")
    Resource resumeAnalysisUserresource;

    public ResumeServiceImpl(
            AIService aiService,
            PositionService positionService,
            ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.positionService = positionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResumeScoreResult scoreResume(String resumeText, String positionType) throws IOException {
        String normalizedPositionType = positionService.normalizePositionType(positionType);
        String positionContext = positionService.buildEvaluationPositionContext(normalizedPositionType);

        String prompt = resumeAnalysisUserresource.getContentAsString(StandardCharsets.UTF_8);
        Map<String, Object> promptVariables = Map.of(
                "resumeText", resumeText,
                "positionType", normalizedPositionType,
                "positionContext", positionContext);

        String response = aiService.executeAiCallWithRetry(
                "简历评分",
                resumeAnalysisSystemPromptResource,
                prompt,
                promptVariables);

        return aiService.parseResumeScoreResult(response);
    }

    @Override
    public ResumeScoreResult scoreResume(String resumeText) throws IOException {
        return scoreResume(resumeText, PositionService.POSITION_BACKEND_JAVA);
    }

    @Override
    public void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult) {
        saveResume(resumeId, resumeText, scoreResult, null, PositionService.POSITION_BACKEND_JAVA);
    }

    @Override
    public void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult, Long userId) {
        saveResume(resumeId, resumeText, scoreResult, userId, PositionService.POSITION_BACKEND_JAVA);
    }

    @Override
    @Transactional
    public void saveResume(
            String resumeId,
            String resumeText,
            ResumeScoreResult scoreResult,
            Long userId,
            String positionType) {
        String normalizedPositionType = positionService.normalizePositionType(positionType);
        if (!isPersistenceEnabled()) {
            LocalDateTime now = LocalDateTime.now();
            resumeStorage.put(resumeId, ResumeData.builder()
                    .resumeId(resumeId).resumeText(resumeText).positionType(normalizedPositionType)
                    .scoreResult(scoreResult)
                    .status(STATUS_ANALYZED).createdAt(now).updatedAt(now).build());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ResumeSessionEntity session = resumeSessionRepository.findByResumeId(resumeId)
                .orElseGet(ResumeSessionEntity::new);
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
        session.getInterviewQuestions().clear();
        session.getEvaluationCategoryScores().clear();
        session.getEvaluationQuestionDetails().clear();
        session.getEvaluationStrengths().clear();
        session.getEvaluationImprovements().clear();
        session.getEvaluationReferenceAnswers().clear();
        resumeSessionRepository.save(session);
    }

    @Override
    public ResumeData getResumeById(String resumeId) {
        return getResumeById(resumeId, null);
    }

    @Override
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

    private boolean isPersistenceEnabled() {
        return resumeSessionRepository != null;
    }

    private void ensureCollections(ResumeSessionEntity s) {
        if (s.getResumeStrengths() == null)
            s.setResumeStrengths(new ArrayList<>());
        if (s.getResumeSuggestions() == null)
            s.setResumeSuggestions(new ArrayList<>());
        if (s.getInterviewQuestions() == null)
            s.setInterviewQuestions(new ArrayList<>());
        if (s.getEvaluationCategoryScores() == null)
            s.setEvaluationCategoryScores(new ArrayList<>());
        if (s.getEvaluationQuestionDetails() == null)
            s.setEvaluationQuestionDetails(new ArrayList<>());
        if (s.getEvaluationStrengths() == null)
            s.setEvaluationStrengths(new ArrayList<>());
        if (s.getEvaluationImprovements() == null)
            s.setEvaluationImprovements(new ArrayList<>());
        if (s.getEvaluationReferenceAnswers() == null)
            s.setEvaluationReferenceAnswers(new ArrayList<>());
    }

    private void replaceResumeStrengths(ResumeSessionEntity s, List<String> strengths) {
        s.getResumeStrengths().clear();
        if (strengths == null)
            return;
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
        if (suggestions == null)
            return;
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

    private ResumeData toResumeData(ResumeSessionEntity s) {
        ResumeScoreResult score = ResumeScoreResult.builder()
                .overallScore(s.getResumeOverallScore())
                .scoreDetail(new ResumeScoreResult.ScoreDetail(
                        s.getScoreProject(), s.getScoreSkillMatch(), s.getScoreContent(), s.getScoreStructure(),
                        s.getScoreExpression()))
                .summary(s.getResumeSummary())
                .strengths(s.getResumeStrengths().stream()
                        .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .map(ResumeStrengthEntity::getStrengthText).collect(java.util.stream.Collectors.toList()))
                .suggestions(s.getResumeSuggestions().stream()
                        .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .map(it -> new ResumeScoreResult.Suggestion(it.getCategory(), it.getPriorityLevel(),
                                it.getIssueText(), it.getRecommendation()))
                        .collect(java.util.stream.Collectors.toList()))
                .build();

        // 构建面试问题
        com.h.resumeagent.common.dto.InterviewQuestions questions = null;
        if (s.getInterviewQuestions() != null && !s.getInterviewQuestions().isEmpty()) {
            java.util.List<com.h.resumeagent.common.dto.InterviewQuestions.Question> questionList = new java.util.ArrayList<>();
            for (com.h.resumeagent.common.entity.InterviewQuestionEntity q : s.getInterviewQuestions()) {
                if (q != null) {
                    com.h.resumeagent.common.dto.InterviewQuestions.Question question = new com.h.resumeagent.common.dto.InterviewQuestions.Question();
                    question.setQuestion(q.getQuestionText());
                    question.setType(q.getQuestionType());
                    question.setCategory(q.getCategory());
                    questionList.add(question);
                }
            }
            if (!questionList.isEmpty()) {
                questions = new com.h.resumeagent.common.dto.InterviewQuestions();
                questions.setQuestions(questionList);
            }
        }

        // 构建评估结果
        com.h.resumeagent.common.dto.InterviewEvaluation evaluation = null;
        if (s.getEvaluationSessionId() != null) {
            evaluation = com.h.resumeagent.common.dto.InterviewEvaluation.builder()
                    .sessionId(s.getEvaluationSessionId())
                    .totalQuestions(s.getEvaluationTotalQuestions())
                    .overallScore(s.getEvaluationOverallScore())
                    .overallFeedback(s.getEvaluationOverallFeedback())
                    .categoryScores(s.getEvaluationCategoryScores().stream()
                            .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                            .map(it -> {
                                com.h.resumeagent.common.dto.InterviewEvaluation.CategoryScore scoreItem = new com.h.resumeagent.common.dto.InterviewEvaluation.CategoryScore();
                                scoreItem.setCategory(it.getCategory());
                                scoreItem.setScore(it.getScore());
                                scoreItem.setQuestionCount(it.getQuestionCount());
                                return scoreItem;
                            })
                            .collect(java.util.stream.Collectors.toList()))
                    .questionDetails(s.getEvaluationQuestionDetails().stream()
                            .map(it -> {
                                com.h.resumeagent.common.dto.InterviewEvaluation.QuestionDetail detail = new com.h.resumeagent.common.dto.InterviewEvaluation.QuestionDetail();
                                detail.setQuestionIndex(it.getQuestionIndex());
                                detail.setQuestion(it.getQuestionText());
                                detail.setCategory(it.getCategory());
                                detail.setUserAnswer(it.getUserAnswer());
                                detail.setScore(it.getScore());
                                detail.setFeedback(it.getFeedback());
                                return detail;
                            })
                            .collect(java.util.stream.Collectors.toList()))
                    .strengths(s.getEvaluationStrengths().stream()
                            .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                            .map(it -> it.getStrengthText())
                            .collect(java.util.stream.Collectors.toList()))
                    .improvements(s.getEvaluationImprovements().stream()
                            .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                            .map(it -> it.getImprovementText())
                            .collect(java.util.stream.Collectors.toList()))
                    .referenceAnswers(s.getEvaluationReferenceAnswers().stream()
                            .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                            .map(it -> {
                                com.h.resumeagent.common.dto.InterviewEvaluation.ReferenceAnswer answer = new com.h.resumeagent.common.dto.InterviewEvaluation.ReferenceAnswer();
                                answer.setQuestionIndex(it.getQuestionIndex());
                                answer.setQuestion(it.getQuestionText());
                                answer.setReferenceAnswer(it.getReferenceAnswer());
                                answer.setKeyPoints(it.getKeyPoints().stream()
                                        .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                                        .map(it2 -> it2.getKeyPoint())
                                        .collect(java.util.stream.Collectors.toList()));
                                return answer;
                            })
                            .collect(java.util.stream.Collectors.toList()))
                    .build();
        }

        return ResumeData.builder()
                .resumeId(s.getResumeId())
                .resumeText(s.getResumeText())
                .positionType(positionService.normalizePositionType(s.getPositionType()))
                .scoreResult(score)
                .questions(questions)
                .evaluation(evaluation)
                .status(s.getStatus() == null ? STATUS_ANALYZED : s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
