package com.h.resumeagent.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 历史记录简要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeHistoryItem {

    private String resumeId;

    private String status;

    private Integer resumeScore;

    private Integer evaluationScore;

    private Integer questionCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
