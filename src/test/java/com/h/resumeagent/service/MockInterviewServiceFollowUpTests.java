package com.h.resumeagent.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MockInterviewServiceFollowUpTests {

    private Object service;
    private Method parseFollowUpMethod;

    @BeforeEach
    void setUp() throws Exception {
        Class<?> serviceClass = Class.forName("com.h.resumeagent.service.MockInterviewService");
        service = serviceClass
                .getConstructor(DashScopeChatModel.class, ObjectMapper.class)
                .newInstance(mock(DashScopeChatModel.class), new ObjectMapper());
        parseFollowUpMethod = serviceClass.getDeclaredMethod("parseFollowUpQuestion", String.class);
        parseFollowUpMethod.setAccessible(true);
    }

    @Test
    void shouldParseJsonFollowUpQuestion() throws Exception {
        String json = "{\"followUpQuestion\":\"你提到用了 Redis，具体如何保证缓存一致性？\"}";
        String result = String.valueOf(parseFollowUpMethod.invoke(service, json));
        assertEquals("你提到用了 Redis，具体如何保证缓存一致性？", result);
    }

    @Test
    void shouldParseCodeBlockJsonFollowUpQuestion() throws Exception {
        String json = """
                ```json
                {"followUpQuestion":"你在项目中如何定位并发死锁问题？"}
                ```
                """;
        String result = String.valueOf(parseFollowUpMethod.invoke(service, json));
        assertEquals("你在项目中如何定位并发死锁问题？", result);
    }
}
