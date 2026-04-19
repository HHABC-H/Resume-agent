package com.h.resumeagent.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AIServiceQuestionParseTests {

    @Test
    void shouldParseQuestionListFieldWhenQuestionsFieldIsMissing() throws Exception {
        Class<?> serviceClass = Class.forName("com.h.resumeagent.service.impl.AIServiceImpl");
        Object service = serviceClass
                .getConstructor(DashScopeChatModel.class, ObjectMapper.class)
                .newInstance(mock(DashScopeChatModel.class), new ObjectMapper());

        String payload = """
                {
                  "questionList": [
                    {
                      "question": "HashMap 什么时候树化？",
                      "type": "JAVA_COLLECTION",
                      "category": "集合"
                    }
                  ]
                }
                """;

        Method parseMethod = serviceClass.getMethod("parseInterviewQuestions", String.class);
        Object interviewQuestions = parseMethod.invoke(service, payload);
        Method getQuestions = interviewQuestions.getClass().getMethod("getQuestions");
        @SuppressWarnings("unchecked")
        List<Object> questions = (List<Object>) getQuestions.invoke(interviewQuestions);

        assertEquals(1, questions.size());
        Object question = questions.get(0);
        assertEquals("HashMap 什么时候树化？", question.getClass().getMethod("getQuestion").invoke(question));
    }
}
