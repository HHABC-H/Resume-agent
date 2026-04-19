package com.h.resumeagent.service.impl;

import com.h.resumeagent.service.PositionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PositionServiceImpl implements PositionService {

    @Override
    public String normalizePositionType(String positionType) {
        if (StringUtils.isBlank(positionType)) {
            return POSITION_BACKEND_JAVA;
        }
        String normalized = positionType.trim().toUpperCase();
        if (POSITION_FRONTEND.equals(normalized) || POSITION_ALGORITHM.equals(normalized)) {
            return normalized;
        }
        return POSITION_BACKEND_JAVA;
    }

    @Override
    public String displayPositionType(String positionType) {
        String normalized = normalizePositionType(positionType);
        return switch (normalized) {
            case POSITION_FRONTEND -> "前端";
            case POSITION_ALGORITHM -> "算法";
            default -> "后端Java";
        };
    }

    @Override
    public String buildQuestionPositionContext(String normalizedPositionType) {
        return switch (normalizedPositionType) {
            case POSITION_FRONTEND ->
                "聚焦 HTML/CSS/JavaScript/TypeScript、Vue/React、浏览器渲染机制、性能优化、前端工程化与调试。";
            case POSITION_ALGORITHM ->
                "聚焦数据结构、算法设计、复杂度分析、边界条件处理、代码正确性与优化思路。";
            default ->
                "聚焦 Java 后端基础、并发、JVM、数据库、缓存、Spring 生态、系统设计与性能优化。";
        };
    }

    @Override
    public String buildEvaluationPositionContext(String normalizedPositionType) {
        return switch (normalizedPositionType) {
            case POSITION_FRONTEND ->
                "重点看前端技术深度、工程化实践、性能与兼容性处理能力、问题定位与用户体验意识。";
            case POSITION_ALGORITHM ->
                "重点看建模能力、算法正确性、复杂度控制、边界分析、表达清晰度和可实现性。";
            default ->
                "重点看后端技术深度、系统设计思维、性能与稳定性意识、工程实践与表达完整性。";
        };
    }
}
