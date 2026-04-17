package com.h.resumeagent.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewFollowUpResponse {
    private Integer questionIndex;
    private String followUpQuestion;
}
