package com.h.resumeagent.service.impl;

import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeHistoryItem;
import com.h.resumeagent.common.entity.ResumeSessionEntity;
import com.h.resumeagent.common.repository.ResumeSessionRepository;
import com.h.resumeagent.mapper.ResumeHistoryMapper;
import com.h.resumeagent.service.HistoryService;
import com.h.resumeagent.service.PositionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl implements HistoryService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryServiceImpl.class);
    private static final String STATUS_ANALYZED = "ANALYZED";

    private final PositionService positionService;
    private final Map<String, ResumeData> resumeStorage = new ConcurrentHashMap<>();
    private volatile boolean historyViewAvailable = true;

    @Autowired(required = false)
    private ResumeSessionRepository resumeSessionRepository;

    @Autowired(required = false)
    private ResumeHistoryMapper resumeHistoryMapper;

    public HistoryServiceImpl(PositionService positionService) {
        this.positionService = positionService;
    }

    @Override
    public List<ResumeHistoryItem> getRecentResumeHistory(int limit) {
        return getRecentResumeHistory(null, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeHistoryItem> getRecentResumeHistory(Long userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        if (!isPersistenceEnabled()) {
            return resumeStorage.values().stream()
                    .sorted(Comparator.comparing(ResumeData::getUpdatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(safeLimit)
                    .map(this::toHistoryItem)
                    .collect(Collectors.toList());
        }
        PageRequest page = PageRequest.of(0, safeLimit);

        if (historyViewAvailable && resumeHistoryMapper != null) {
            try {
                List<ResumeHistoryItem> rows = userId == null
                        ? resumeHistoryMapper.selectRecentHistory(safeLimit)
                        : resumeHistoryMapper.selectRecentHistoryByUserId(userId, safeLimit);
                return rows.stream().map(this::normalizeHistoryItem).collect(Collectors.toList());
            } catch (RuntimeException ex) {
                if (isHistoryViewSchemaException(ex)) {
                    historyViewAvailable = false;
                    logger.warn("历史视图查询失败，已降级为 resume_session 查询: {}", shortMessage(ex));
                } else {
                    throw ex;
                }
            }
        }

        List<ResumeSessionEntity> sessions = userId == null
                ? resumeSessionRepository.findAllByOrderByUpdatedAtDesc(page)
                : resumeSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, page);
        return sessions.stream().map(this::toHistoryItem).collect(Collectors.toList());
    }

    private boolean isPersistenceEnabled() {
        return resumeSessionRepository != null;
    }

    private ResumeHistoryItem toHistoryItem(ResumeData d) {
        int questionCount = d.getQuestions() == null || d.getQuestions().getQuestions() == null ? 0
                : d.getQuestions().getQuestions().size();
        Integer evalScore = d.getEvaluation() == null ? null : d.getEvaluation().getOverallScore();
        Integer resumeScore = d.getScoreResult() == null ? null : d.getScoreResult().getOverallScore();
        return ResumeHistoryItem.builder()
                .resumeId(d.getResumeId()).status(StringUtils.defaultIfBlank(d.getStatus(), STATUS_ANALYZED))
                .positionType(positionService.normalizePositionType(d.getPositionType()))
                .resumeScore(resumeScore).evaluationScore(evalScore).questionCount(questionCount)
                .createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
    }

    private ResumeHistoryItem normalizeHistoryItem(ResumeHistoryItem item) {
        if (item == null) {
            return null;
        }
        item.setStatus(StringUtils.defaultIfBlank(item.getStatus(), STATUS_ANALYZED));
        item.setPositionType(positionService.normalizePositionType(item.getPositionType()));
        if (item.getQuestionCount() == null) {
            item.setQuestionCount(0);
        }
        return item;
    }

    private ResumeHistoryItem toHistoryItem(ResumeSessionEntity s) {
        int questionCount = s.getInterviewQuestions() == null ? 0 : s.getInterviewQuestions().size();
        return ResumeHistoryItem.builder()
                .resumeId(s.getResumeId())
                .status(s.getStatus() == null ? STATUS_ANALYZED : s.getStatus().name())
                .positionType(positionService.normalizePositionType(s.getPositionType()))
                .resumeScore(s.getResumeOverallScore())
                .evaluationScore(s.getEvaluationOverallScore())
                .questionCount(questionCount)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private boolean isHistoryViewSchemaException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && (
                    message.contains("Table") && message.contains("doesn't exist") ||
                    message.contains("列名无效") ||
                    message.contains("column") && message.contains("not found")
            )) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String shortMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (StringUtils.isNotBlank(message)) {
            return message;
        }
        return throwable.getClass().getSimpleName();
    }
}
