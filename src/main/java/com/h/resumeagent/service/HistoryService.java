package com.h.resumeagent.service;

import com.h.resumeagent.common.dto.PageResponse;
import com.h.resumeagent.common.dto.ResumeHistoryItem;

import java.util.List;

public interface HistoryService {

    List<ResumeHistoryItem> getRecentResumeHistory(int limit);

    List<ResumeHistoryItem> getRecentResumeHistory(Long userId, int limit);

    PageResponse<ResumeHistoryItem> getResumeHistoryPage(int page, int size);

    PageResponse<ResumeHistoryItem> getResumeHistoryPage(Long userId, int page, int size);
}
