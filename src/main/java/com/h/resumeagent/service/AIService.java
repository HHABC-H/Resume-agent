package com.h.resumeagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import org.springframework.core.io.Resource;

import java.util.Map;

public interface AIService {

    String executeAiCallWithRetry(
            String scene,
            Resource systemPromptResource,
            String userPrompt,
            Map<String, Object> variables);

    ResumeScoreResult parseResumeScoreResult(String json) throws JsonProcessingException;

    InterviewQuestions parseInterviewQuestions(String json) throws JsonProcessingException;

    String parseFollowUpQuestion(String json) throws JsonProcessingException;

    InterviewEvaluation parseInterviewEvaluation(String json) throws JsonProcessingException;
}
