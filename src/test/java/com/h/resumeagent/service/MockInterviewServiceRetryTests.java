package com.h.resumeagent.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MockInterviewServiceRetryTests {

    private Object service;
    private Class<?> serviceClass;
    private Method executeWithRetryMethod;

    @BeforeEach
    void setUp() throws Exception {
        serviceClass = Class.forName("com.h.resumeagent.service.impl.MockInterviewServiceImpl");
        service = serviceClass
                .getConstructor(DashScopeChatModel.class, ObjectMapper.class)
                .newInstance(mock(DashScopeChatModel.class), new ObjectMapper());
        executeWithRetryMethod = serviceClass.getDeclaredMethod("executeAiCallWithRetry", String.class, Supplier.class);
        executeWithRetryMethod.setAccessible(true);
        setField("aiRetryMaxAttempts", 3);
        setField("aiRetryBackoffMs", 1L);
        setField("aiRetryMaxBackoffMs", 2L);
    }

    @Test
    void shouldRetryTransientConnectionErrorAndEventuallySucceed() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = String.valueOf(invokeExecuteWithRetry("测试调用", () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new ResourceAccessException("Connection reset by peer");
            }
            return "ok";
        }));

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void shouldNotRetryNonTransientException() {
        AtomicInteger attempts = new AtomicInteger(0);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            invokeExecuteWithRetry("测试调用", () -> {
                attempts.incrementAndGet();
                throw new IllegalArgumentException("bad request");
            });
        });

        assertEquals(IllegalArgumentException.class, runtimeException.getCause().getClass());
        assertEquals(1, attempts.get());
    }

    private Object invokeExecuteWithRetry(String scene, Supplier<String> supplier) {
        try {
            return executeWithRetryMethod.invoke(service, scene, supplier);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause() == null ? e : e.getCause());
        }
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = serviceClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
