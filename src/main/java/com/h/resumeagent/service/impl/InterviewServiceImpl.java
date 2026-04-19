package com.h.resumeagent.service.impl;

import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.entity.*;
import com.h.resumeagent.common.repository.ResumeSessionRepository;
import com.h.resumeagent.service.AIService;
import com.h.resumeagent.service.InterviewService;
import com.h.resumeagent.service.PositionService;
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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InterviewServiceImpl implements InterviewService {
    private static final Logger logger = LoggerFactory.getLogger(InterviewServiceImpl.class);
    private static final String STATUS_QUESTIONS_READY = "QUESTIONS_READY";
    private static final String STATUS_EVALUATED = "EVALUATED";

    private final AIService aiService;
    private final PositionService positionService;
    private final Map<String, Object> interviewStorage = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private ResumeSessionRepository resumeSessionRepository;

    @Value("classpath:/prompt/interview-evaluation-system.st")
    Resource interviewEvaluationSystemPromptresource;

    @Value("classpath:/prompt/interview-question-system.st")
    Resource interviewQuestionsSystemPromptresource;

    @Value("classpath:/prompt/interview-followup-system.st")
    Resource interviewFollowupSystemPromptResource;

    public InterviewServiceImpl(
            AIService aiService,
            PositionService positionService) {
        this.aiService = aiService;
        this.positionService = positionService;
    }

    @Override
    public InterviewQuestions generateInterviewQuestions(String resumeText, String positionType) {
        return generateInterviewQuestions(resumeText, positionType, 10);
    }

    @Override
    public InterviewQuestions generateInterviewQuestions(String resumeText, String positionType, int questionCount) {
        int safeQuestionCount = Math.max(1, Math.min(questionCount, 20));
        logger.info("开始生成面试问题，positionType: {}, resumeText长度: {}, 题目数量: {}", positionType,
                resumeText != null ? resumeText.length() : 0, safeQuestionCount);
        String normalizedPositionType = positionService.normalizePositionType(positionType);
        String positionContext = positionService.buildQuestionPositionContext(normalizedPositionType);

        logger.info("标准化后的岗位类型: {}, 岗位上下文长度: {}", normalizedPositionType,
                positionContext != null ? positionContext.length() : 0);

        String userPrompt = """
                请根据以下简历内容生成%d个面试问题：
                目标岗位：%s
                岗位要求：%s
                ## 候选人简历
                %s
                """.formatted(
                safeQuestionCount,
                normalizedPositionType,
                positionContext,
                resumeText);

        logger.info("生成的用户提示词长度: {}", userPrompt.length());

        try {
            logger.info("开始调用AI服务生成面试问题");
            String response = aiService.executeAiCallWithRetry(
                    "面试问题生成",
                    interviewQuestionsSystemPromptresource,
                    userPrompt,
                    Map.of());
            logger.info("AI服务响应长度: {}", response != null ? response.length() : 0);
            logger.info("AI服务响应内容: {}", response);

            InterviewQuestions questions = aiService.parseInterviewQuestions(response);
            logger.info("解析后的面试问题数量: {}",
                    questions != null && questions.getQuestions() != null ? questions.getQuestions().size() : 0);
            if (questions == null || questions.getQuestions() == null || questions.getQuestions().isEmpty()) {
                throw new IllegalStateException("AI未能生成任何有效面试问题");
            }
            return questions;
        } catch (Exception e) {
            logger.error("生成面试问题失败", e);
            throw new RuntimeException("生成面试问题失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateFollowUpQuestion(
            String resumeText,
            String positionType,
            InterviewQuestions.Question question,
            String answer) {
        if (question == null || StringUtils.isBlank(question.getQuestion())) {
            throw new IllegalArgumentException("当前题目信息无效");
        }
        if (StringUtils.isBlank(answer)) {
            throw new IllegalArgumentException("请先回答当前题目，再生成追问");
        }

        String normalizedPositionType = positionService.normalizePositionType(positionType);
        String positionContext = positionService.buildEvaluationPositionContext(normalizedPositionType);

        String userPrompt = """
                目标岗位：%s
                岗位侧重点：%s
                候选人简历摘要：
                %s

                当前面试题：
                %s

                候选人当前回答：
                %s
                """.formatted(
                normalizedPositionType,
                positionContext,
                StringUtils.defaultIfBlank(resumeText, "简历内容为空"),
                question.getQuestion(),
                answer);

        try {
            String response = aiService.executeAiCallWithRetry(
                    "面试追问生成",
                    interviewFollowupSystemPromptResource,
                    userPrompt,
                    Map.of());
            return aiService.parseFollowUpQuestion(response);
        } catch (Exception e) {
            logger.error("生成追问失败", e);
            throw new RuntimeException("生成追问失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InterviewEvaluation evaluateAnswers(
            String resumeText,
            String positionType,
            InterviewQuestions questions,
            Map<Integer, String> answers) {
        return evaluateAnswers(resumeText, positionType, questions, answers, null);
    }

    @Override
    public InterviewEvaluation evaluateAnswers(
            String resumeText,
            String positionType,
            InterviewQuestions questions,
            Map<Integer, String> answers,
            Map<Integer, String> followUpAnswers) {
        String normalizedPositionType = positionService.normalizePositionType(positionType);
        String positionContext = positionService.buildEvaluationPositionContext(normalizedPositionType);

        StringBuilder qaText = new StringBuilder();
        for (int i = 0; i < questions.getQuestions().size(); i++) {
            InterviewQuestions.Question q = questions.getQuestions().get(i);
            String answer = StringUtils.isBlank(answers.get(i)) ? "未作答" : answers.get(i);
            qaText.append("问题 %d [%s]: %s%n".formatted(i + 1, q.getType(), q.getQuestion()));
            qaText.append("候选人回答：%s%n".formatted(answer));
            // 添加追问答案
            if (followUpAnswers != null && followUpAnswers.containsKey(i)) {
                String followUpAnswer = StringUtils.isBlank(followUpAnswers.get(i)) ? "未作答" : followUpAnswers.get(i);
                qaText.append("追问回答：%s%n".formatted(followUpAnswer));
            }
            qaText.append("\n");
        }

        String userPrompt = """
                请评估以下面试问答：
                目标岗位：%s
                评分侧重点：%s
                %s
                """.formatted(
                normalizedPositionType,
                positionContext,
                qaText);

        try {
            String response = aiService.executeAiCallWithRetry(
                    "面试答案评估",
                    interviewEvaluationSystemPromptresource,
                    userPrompt,
                    Map.of());
            return aiService.parseInterviewEvaluation(response);
        } catch (Exception e) {
            logger.error("评估面试答案失败", e);
            throw new RuntimeException("评估面试答案失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void saveQuestions(String resumeId, InterviewQuestions questions) {
        if (!isPersistenceEnabled()) {
            // 内存存储逻辑
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

    @Override
    @Transactional
    public void saveEvaluation(String resumeId, InterviewEvaluation evaluation) {
        if (!isPersistenceEnabled()) {
            // 内存存储逻辑
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

    private boolean isPersistenceEnabled() {
        return resumeSessionRepository != null;
    }

    private void ensureCollections(ResumeSessionEntity s) {
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

    private void replaceInterviewQuestions(ResumeSessionEntity s, List<InterviewQuestions.Question> questions) {
        s.getInterviewQuestions().clear();
        if (questions == null)
            return;
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
        if (list == null)
            return;
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
        if (list == null)
            return;
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
        if (list == null)
            return;
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
        if (list == null)
            return;
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
        if (list == null)
            return;
        for (int i = 0; i < list.size(); i++) {
            InterviewEvaluation.ReferenceAnswer it = list.get(i);
            EvaluationReferenceAnswerEntity e = new EvaluationReferenceAnswerEntity();
            e.setResumeSession(s);
            e.setQuestionIndex(it == null ? null : it.getQuestionIndex());
            e.setQuestionText(it == null ? null : it.getQuestion());
            e.setReferenceAnswer(it == null ? null : it.getReferenceAnswer());
            e.setSortOrder(i);
            List<EvaluationReferenceKeyPointEntity> points = new ArrayList<>();
            List<String> keys = it == null ? List.of() : it.getKeyPoints();
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
}
