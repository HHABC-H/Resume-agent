package com.h.resumeagent.service;

public interface PositionService {

    String POSITION_BACKEND_JAVA = "BACKEND_JAVA";
    String POSITION_FRONTEND = "FRONTEND";
    String POSITION_ALGORITHM = "ALGORITHM";

    String normalizePositionType(String positionType);

    String displayPositionType(String positionType);

    String buildQuestionPositionContext(String normalizedPositionType);

    String buildEvaluationPositionContext(String normalizedPositionType);
}
