package com.h.resumeagent.service;

import com.h.resumeagent.common.dto.ResumeEditDTO;
import com.h.resumeagent.common.dto.SaveResumeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResumeEditService {
    ResumeEditDTO saveDraft(Long userId, SaveResumeRequest request);
    ResumeEditDTO updateDraft(Long userId, SaveResumeRequest request);
    ResumeEditDTO getCurrentDraft(Long userId);
    ResumeEditDTO saveAsVersion(Long userId, SaveResumeRequest request);
    Page<ResumeEditDTO> getHistory(Long userId, Pageable pageable);
    ResumeEditDTO getHistoryDetail(Long id, Long userId);
    void deleteHistory(Long id, Long userId);
}