package com.h.resumeagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.PageResponse;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeHistoryItem;
import com.h.resumeagent.common.dto.ResumeScoreResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MockInterviewService {

    // 简历相关方法
    ResumeScoreResult scoreResume(String resumeText) throws IOException;

    ResumeScoreResult scoreResume(String resumeText, String positionType) throws IOException;

    void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult);

    void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult, Long userId);

    void saveResume(
            String resumeId,
            String resumeText,
            ResumeScoreResult scoreResult,
            Long userId,
            String positionType);

    ResumeData getResumeById(String resumeId);

    ResumeData getResumeById(String resumeId, Long userId);

    // 面试相关方法
    InterviewQuestions generateInterviewQuestions(String resumeText, String positionType) throws JsonProcessingException;

    InterviewQuestions generateInterviewQuestions(String resumeText, String positionType, int questionCount) throws JsonProcessingException;

    String generateFollowUpQuestion(
            String resumeText,
            String positionType,
            InterviewQuestions.Question question,
            String answer) throws JsonProcessingException;

    InterviewEvaluation evaluateAnswers(
            String resumeText,
            String positionType,
            InterviewQuestions questions,
            Map<Integer, String> answers) throws JsonProcessingException;

    InterviewEvaluation evaluateAnswers(
            String resumeText,
            String positionType,
            InterviewQuestions questions,
            Map<Integer, String> answers,
            Map<Integer, String> followUpAnswers) throws JsonProcessingException;

    void saveQuestions(String resumeId, InterviewQuestions questions);

    void saveEvaluation(String resumeId, InterviewEvaluation evaluation);

    // 历史记录相关方法
    List<ResumeHistoryItem> getRecentResumeHistory(int limit);

    List<ResumeHistoryItem> getRecentResumeHistory(Long userId, int limit);

    PageResponse<ResumeHistoryItem> getResumeHistoryPage(int page, int size);

    PageResponse<ResumeHistoryItem> getResumeHistoryPage(Long userId, int page, int size);

    // 职位类型相关方法
    String normalizePositionType(String positionType);

    String displayPositionType(String positionType);
}
