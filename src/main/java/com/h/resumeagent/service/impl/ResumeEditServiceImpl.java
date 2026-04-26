package com.h.resumeagent.service.impl;

import com.h.resumeagent.common.dto.ResumeEditDTO;
import com.h.resumeagent.common.dto.SaveResumeRequest;
import com.h.resumeagent.common.entity.ResumeEditSessionEntity;
import com.h.resumeagent.common.repository.ResumeEditSessionRepository;
import com.h.resumeagent.service.ResumeEditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class ResumeEditServiceImpl implements ResumeEditService {

    private final ResumeEditSessionRepository repository;
    private final ObjectMapper objectMapper;

    public ResumeEditServiceImpl(ResumeEditSessionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResumeEditDTO saveDraft(Long userId, SaveResumeRequest request) {
        ResumeEditSessionEntity entity = ResumeEditSessionEntity.builder()
                .userId(userId)
                .structuredData(toJson(request.getStructuredData()))
                .resumeText(request.getResumeText())
                .status("DRAFT")
                .build();
        entity = repository.save(entity);
        return toDTO(entity);
    }

    @Override
    public ResumeEditDTO updateDraft(Long userId, SaveResumeRequest request) {
        ResumeEditSessionEntity entity = repository.findByUserIdAndStatus(userId, "DRAFT")
                .orElseThrow(() -> new RuntimeException("Draft not found"));
        entity.setStructuredData(toJson(request.getStructuredData()));
        entity.setResumeText(request.getResumeText());
        entity = repository.save(entity);
        return toDTO(entity);
    }

    @Override
    public ResumeEditDTO getCurrentDraft(Long userId) {
        return repository.findByUserIdAndStatus(userId, "DRAFT")
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public ResumeEditDTO saveAsVersion(Long userId, SaveResumeRequest request) {
        repository.findByUserIdAndStatus(userId, "DRAFT").ifPresent(draft -> {
            draft.setStatus("PUBLISHED");
            repository.save(draft);
        });

        ResumeEditSessionEntity entity = ResumeEditSessionEntity.builder()
                .userId(userId)
                .structuredData(toJson(request.getStructuredData()))
                .resumeText(request.getResumeText())
                .status("PUBLISHED")
                .build();
        entity = repository.save(entity);
        return toDTO(entity);
    }

    @Override
    public Page<ResumeEditDTO> getHistory(Long userId, Pageable pageable) {
        return repository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "PUBLISHED", pageable)
                .map(this::toDTO);
    }

    @Override
    public ResumeEditDTO getHistoryDetail(Long id, Long userId) {
        ResumeEditSessionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        return toDTO(entity);
    }

    @Override
    public void deleteHistory(Long id, Long userId) {
        ResumeEditSessionEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        repository.delete(entity);
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize structured data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize structured data", e);
        }
    }

    private ResumeEditDTO toDTO(ResumeEditSessionEntity entity) {
        return ResumeEditDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .structuredData(fromJson(entity.getStructuredData()))
                .resumeText(entity.getResumeText())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}