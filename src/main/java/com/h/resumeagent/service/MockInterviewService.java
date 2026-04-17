package com.h.resumeagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeHistoryItem;
import com.h.resumeagent.common.dto.ResumeScoreResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MockInterviewService {

    String POSITION_BACKEND_JAVA = "BACKEND_JAVA";
    String POSITION_FRONTEND = "FRONTEND";
    String POSITION_ALGORITHM = "ALGORITHM";

    ResumeScoreResult scoreResume(String resumeText) throws IOException;

    InterviewQuestions generateInterviewQuestions(String resumeText, String positionType) throws JsonProcessingException;

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

    void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult);

    void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult, Long userId);

    void saveResume(
            String resumeId,
            String resumeText,
            ResumeScoreResult scoreResult,
            Long userId,
            String positionType);

    void saveQuestions(String resumeId, InterviewQuestions questions);

    void saveEvaluation(String resumeId, InterviewEvaluation evaluation);

    ResumeData getResumeById(String resumeId);

    ResumeData getResumeById(String resumeId, Long userId);

    List<ResumeHistoryItem> getRecentResumeHistory(int limit);

    List<ResumeHistoryItem> getRecentResumeHistory(Long userId, int limit);

    String normalizePositionType(String positionType);

    String displayPositionType(String positionType);
}
