package com.h.resumeagent.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiLogger {
    private static final Logger logger = LoggerFactory.getLogger(ApiLogger.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Map<String, ApiCallInfo> activeCalls = new ConcurrentHashMap<>();

    public void logApiCallStart(String scene, String callId) {
        String timestamp = LocalDateTime.now().format(formatter);
        ApiCallInfo info = new ApiCallInfo(scene, callId, timestamp);
        activeCalls.put(callId, info);
        logger.info("[API_CALL_START] scene={}, callId={}, timestamp={}", scene, callId, timestamp);
    }

    public void logApiCallEnd(String scene, String callId, String result, boolean success) {
        ApiCallInfo info = activeCalls.remove(callId);
        String timestamp = LocalDateTime.now().format(formatter);
        if (info != null) {
            long duration = System.currentTimeMillis() - info.startTime;
            if (success) {
                logger.info("[API_CALL_END] scene={}, callId={}, timestamp={}, duration={}ms, resultLength={}, success=true",
                        scene, callId, timestamp, duration, result != null ? result.length() : 0);
            } else {
                logger.error("[API_CALL_END] scene={}, callId={}, timestamp={}, duration={}ms, success=false, error={}",
                        scene, callId, timestamp, duration, result);
            }
        } else {
            if (success) {
                logger.info("[API_CALL_END] scene={}, callId={}, timestamp={}, resultLength={}, success=true",
                        scene, callId, timestamp, result != null ? result.length() : 0);
            } else {
                logger.error("[API_CALL_END] scene={}, callId={}, timestamp={}, success=false, error={}",
                        scene, callId, timestamp, result);
            }
        }
    }

    public void logApiCallError(String scene, String callId, Throwable error) {
        ApiCallInfo info = activeCalls.remove(callId);
        String timestamp = LocalDateTime.now().format(formatter);
        String errorMsg = error != null ? error.getMessage() : "Unknown error";
        if (info != null) {
            long duration = System.currentTimeMillis() - info.startTime;
            logger.error("[API_CALL_ERROR] scene={}, callId={}, timestamp={}, duration={}ms, errorClass={}, errorMsg={}",
                    scene, callId, timestamp, duration,
                    error != null ? error.getClass().getSimpleName() : "null",
                    errorMsg);
        } else {
            logger.error("[API_CALL_ERROR] scene={}, callId={}, timestamp={}, errorClass={}, errorMsg={}",
                    scene, callId, timestamp,
                    error != null ? error.getClass().getSimpleName() : "null",
                    errorMsg);
        }
    }

    private static class ApiCallInfo {
        final String scene;
        final String callId;
        final String timestamp;
        final long startTime;

        ApiCallInfo(String scene, String callId, String timestamp) {
            this.scene = scene;
            this.callId = callId;
            this.timestamp = timestamp;
            this.startTime = System.currentTimeMillis();
        }
    }
}