package com.h.resumeagent.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MockInterviewServiceHistoryTests {

    private Object service;
    private Class<?> serviceClass;

    @BeforeEach
    void setUp() throws Exception {
        serviceClass = Class.forName("com.h.resumeagent.service.impl.MockInterviewServiceImpl");
        service = serviceClass
                .getConstructor(DashScopeChatModel.class, ObjectMapper.class)
                .newInstance(mock(DashScopeChatModel.class), new ObjectMapper());
    }

    @Test
    void shouldReturnHistoryOrderedByLatestUpdate() throws Exception {
        invoke("saveResume", "resume-1", "Java resume", createScore(82));
        invoke("saveResume", "resume-2", "Spring resume", createScore(75));

        Object questions = createQuestions("讲讲线程池", "JAVA_CONCURRENT", "并发");
        invoke("saveQuestions", "resume-1", questions);

        List<?> history = getHistory(10);

        assertEquals(2, history.size());
        assertEquals("resume-1", getString(history.get(0), "getResumeId"));
        assertEquals("QUESTIONS_READY", getString(history.get(0), "getStatus"));
        assertEquals("resume-2", getString(history.get(1), "getResumeId"));
        assertEquals("ANALYZED", getString(history.get(1), "getStatus"));
    }

    @Test
    void shouldRespectHistoryLimitAndEvaluationStatus() throws Exception {
        invoke("saveResume", "resume-a", "A", createScore(60));
        invoke("saveResume", "resume-b", "B", createScore(61));
        invoke("saveResume", "resume-c", "C", createScore(62));

        Object evaluation = createEvaluation(90, "表现优秀");
        invoke("saveEvaluation", "resume-b", evaluation);

        List<?> history = getHistory(2);

        assertEquals(2, history.size());
        assertEquals("resume-b", getString(history.get(0), "getResumeId"));
        assertEquals("EVALUATED", getString(history.get(0), "getStatus"));
    }

    private void invoke(String methodName, String resumeId, Object arg) throws Exception {
        Method method = serviceClass.getMethod(methodName, String.class, arg.getClass());
        method.invoke(service, resumeId, arg);
    }

    private void invoke(String methodName, String resumeId, String resumeText, Object score) throws Exception {
        Class<?> scoreClass = Class.forName("com.h.resumeagent.common.dto.ResumeScoreResult");
        Method method = serviceClass.getMethod(methodName, String.class, String.class, scoreClass);
        method.invoke(service, resumeId, resumeText, score);
    }

    @SuppressWarnings("unchecked")
    private List<?> getHistory(int limit) throws Exception {
        Method method = serviceClass.getMethod("getRecentResumeHistory", int.class);
        return (List<?>) method.invoke(service, limit);
    }

    private Object createScore(int score) throws Exception {
        Class<?> clazz = Class.forName("com.h.resumeagent.common.dto.ResumeScoreResult");
        Object builder = clazz.getMethod("builder").invoke(null);
        Method overallScore = builder.getClass().getMethod("overallScore", Integer.class);
        overallScore.invoke(builder, score);
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private Object createQuestions(String question, String type, String category) throws Exception {
        Class<?> questionClass = Class.forName("com.h.resumeagent.common.dto.InterviewQuestions$Question");
        Object questionObject = questionClass
                .getConstructor(String.class, String.class, String.class)
                .newInstance(question, type, category);

        Class<?> interviewQuestionsClass = Class.forName("com.h.resumeagent.common.dto.InterviewQuestions");
        Object builder = interviewQuestionsClass.getMethod("builder").invoke(null);
        builder.getClass().getMethod("questions", List.class).invoke(builder, List.of(questionObject));
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private Object createEvaluation(int score, String feedback) throws Exception {
        Class<?> clazz = Class.forName("com.h.resumeagent.common.dto.InterviewEvaluation");
        Object builder = clazz.getMethod("builder").invoke(null);
        builder.getClass().getMethod("overallScore", Integer.class).invoke(builder, score);
        builder.getClass().getMethod("overallFeedback", String.class).invoke(builder, feedback);
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private String getString(Object target, String methodName) throws Exception {
        return String.valueOf(target.getClass().getMethod(methodName).invoke(target));
    }
}
