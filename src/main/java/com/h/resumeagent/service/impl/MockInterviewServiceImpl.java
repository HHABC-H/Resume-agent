package com.h.resumeagent.service.impl;

import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeHistoryItem;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import com.h.resumeagent.service.AIService;
import com.h.resumeagent.service.HistoryService;
import com.h.resumeagent.service.InterviewService;
import com.h.resumeagent.service.MockInterviewService;
import com.h.resumeagent.service.PositionService;
import com.h.resumeagent.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class MockInterviewServiceImpl implements MockInterviewService {

    private final ResumeService resumeService;
    private final InterviewService interviewService;
    private final HistoryService historyService;
    private final PositionService positionService;

    @Autowired
    public MockInterviewServiceImpl(
            ResumeService resumeService,
            InterviewService interviewService,
            HistoryService historyService,
            PositionService positionService) {
        this.resumeService = resumeService;
        this.interviewService = interviewService;
        this.historyService = historyService;
        this.positionService = positionService;
    }

    // 简历相关方法
    @Override
    public ResumeScoreResult scoreResume(String resumeText) throws IOException {
        return resumeService.scoreResume(resumeText);
    }
    
    @Override
    public ResumeScoreResult scoreResume(String resumeText, String positionType) throws IOException {
        return resumeService.scoreResume(resumeText, positionType);
    }

    @Override
    public void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult) {
        resumeService.saveResume(resumeId, resumeText, scoreResult);
    }

    @Override
    public void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult, Long userId) {
        resumeService.saveResume(resumeId, resumeText, scoreResult, userId);
    }

    @Override
    public void saveResume(
            String resumeId,
            String resumeText,
            ResumeScoreResult scoreResult,
            Long userId,
            String positionType) {
        resumeService.saveResume(resumeId, resumeText, scoreResult, userId, positionType);
    }

    @Override
    public ResumeData getResumeById(String resumeId) {
        return resumeService.getResumeById(resumeId);
    }

    @Override
    public ResumeData getResumeById(String resumeId, Long userId) {
        return resumeService.getResumeById(resumeId, userId);
    }

    // 面试相关方法
    @Override
    public InterviewQuestions generateInterviewQuestions(String resumeText, String positionType) throws com.fasterxml.jackson.core.JsonProcessingException {
        return interviewService.generateInterviewQuestions(resumeText, positionType);
    }
    
    @Override
    public InterviewQuestions generateInterviewQuestions(String resumeText, String positionType, int questionCount) throws com.fasterxml.jackson.core.JsonProcessingException {
        return interviewService.generateInterviewQuestions(resumeText, positionType, questionCount);
    }

    @Override
    public String generateFollowUpQuestion(
            String resumeText,
            String positionType,
            InterviewQuestions.Question question,
            String answer) throws com.fasterxml.jackson.core.JsonProcessingException {
        return interviewService.generateFollowUpQuestion(resumeText, positionType, question, answer);
    }

    @Override
    public InterviewEvaluation evaluateAnswers(
            String resumeText,
            String positionType,
            InterviewQuestions questions,
            Map<Integer, String> answers) throws com.fasterxml.jackson.core.JsonProcessingException {
        return interviewService.evaluateAnswers(resumeText, positionType, questions, answers);
    }
    
    @Override
    public InterviewEvaluation evaluateAnswers(
            String resumeText,
            String positionType,
            InterviewQuestions questions,
            Map<Integer, String> answers,
            Map<Integer, String> followUpAnswers) throws com.fasterxml.jackson.core.JsonProcessingException {
        return interviewService.evaluateAnswers(resumeText, positionType, questions, answers, followUpAnswers);
    }

    @Override
    public void saveQuestions(String resumeId, InterviewQuestions questions) {
        interviewService.saveQuestions(resumeId, questions);
    }

    @Override
    public void saveEvaluation(String resumeId, InterviewEvaluation evaluation) {
        interviewService.saveEvaluation(resumeId, evaluation);
    }

    // 历史记录相关方法
    @Override
    public List<ResumeHistoryItem> getRecentResumeHistory(int limit) {
        return historyService.getRecentResumeHistory(limit);
    }

    @Override
    public List<ResumeHistoryItem> getRecentResumeHistory(Long userId, int limit) {
        return historyService.getRecentResumeHistory(userId, limit);
    }

    // 职位类型相关方法
    @Override
    public String normalizePositionType(String positionType) {
        return positionService.normalizePositionType(positionType);
    }

    @Override
    public String displayPositionType(String positionType) {
        return positionService.displayPositionType(positionType);
    }
}
