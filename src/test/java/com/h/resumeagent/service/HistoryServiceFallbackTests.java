package com.h.resumeagent.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryServiceFallbackTests {

    @Test
    void shouldFallbackToResumeSessionWhenHistoryViewHasUnknownColumnError() throws Exception {
        Class<?> positionServiceClass = Class.forName("com.h.resumeagent.service.PositionService");
        Object positionService = Proxy.newProxyInstance(
                positionServiceClass.getClassLoader(),
                new Class<?>[] { positionServiceClass },
                (proxy, method, args) -> {
                    if ("normalizePositionType".equals(method.getName())) {
                        return args[0];
                    }
                    if ("displayPositionType".equals(method.getName())) {
                        return args[0];
                    }
                    if ("buildQuestionPositionContext".equals(method.getName())
                            || "buildEvaluationPositionContext".equals(method.getName())) {
                        return "";
                    }
                    return null;
                });

        Class<?> historyServiceImplClass = Class.forName("com.h.resumeagent.service.impl.HistoryServiceImpl");
        Object service = historyServiceImplClass.getConstructor(positionServiceClass).newInstance(positionService);

        Class<?> historyMapperClass = Class.forName("com.h.resumeagent.mapper.ResumeHistoryMapper");
        Object historyMapper = Proxy.newProxyInstance(
                historyMapperClass.getClassLoader(),
                new Class<?>[] { historyMapperClass },
                (proxy, method, args) -> {
                    if ("selectRecentHistoryByUserId".equals(method.getName())) {
                        throw new RuntimeException("Unknown column 'question_count' in 'field list'");
                    }
                    return List.of();
                });

        Class<?> resumeSessionEntityClass = Class.forName("com.h.resumeagent.common.entity.ResumeSessionEntity");
        Object session = resumeSessionEntityClass.getConstructor().newInstance();
        resumeSessionEntityClass.getMethod("setResumeId", String.class).invoke(session, "resume-1");

        Class<?> resumeStatusClass = Class.forName("com.h.resumeagent.common.entity.ResumeStatus");
        Object analyzed = Enum.valueOf((Class<Enum>) resumeStatusClass, "ANALYZED");
        resumeSessionEntityClass.getMethod("setStatus", resumeStatusClass).invoke(session, analyzed);
        resumeSessionEntityClass.getMethod("setPositionType", String.class).invoke(session, "BACKEND_JAVA");
        resumeSessionEntityClass.getMethod("setInterviewQuestions", List.class).invoke(session, new ArrayList<>());
        resumeSessionEntityClass.getMethod("setCreatedAt", LocalDateTime.class)
                .invoke(session, LocalDateTime.now().minusMinutes(2));
        resumeSessionEntityClass.getMethod("setUpdatedAt", LocalDateTime.class)
                .invoke(session, LocalDateTime.now().minusMinutes(1));

        Class<?> resumeSessionRepositoryClass = Class.forName("com.h.resumeagent.common.repository.ResumeSessionRepository");
        Object resumeSessionRepository = Proxy.newProxyInstance(
                resumeSessionRepositoryClass.getClassLoader(),
                new Class<?>[] { resumeSessionRepositoryClass },
                (proxy, method, args) -> {
                    if ("findByUserIdOrderByUpdatedAtDesc".equals(method.getName())) {
                        return List.of(session);
                    }
                    if ("findAllByOrderByUpdatedAtDesc".equals(method.getName())) {
                        return List.of(session);
                    }
                    return null;
                });

        setField(service, "resumeHistoryMapper", historyMapper);
        setField(service, "resumeSessionRepository", resumeSessionRepository);

        Method getRecentHistory = historyServiceImplClass.getMethod("getRecentResumeHistory", Long.class, int.class);
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) getRecentHistory.invoke(service, 1L, 20);

        assertEquals(1, result.size());
        Object item = result.get(0);
        assertEquals("resume-1", item.getClass().getMethod("getResumeId").invoke(item));
        assertEquals("ANALYZED", item.getClass().getMethod("getStatus").invoke(item));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
