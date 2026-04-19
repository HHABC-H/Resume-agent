package com.h.resumeagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewQuestions;

import java.util.Map;

public interface InterviewService {

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
}
