package com.h.resumeagent.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 简历数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeData {
    
    /**
     * 简历 ID
     */
    private String resumeId;
    
    /**
     * 简历文本内容
     */
    private String resumeText;

    /**
     * 面试岗位类型：BACKEND_JAVA / FRONTEND / ALGORITHM
     */
    private String positionType;
    
    /**
     * 评分结果
     */
    private ResumeScoreResult scoreResult;
    
    /**
     * 面试问题
     */
    private InterviewQuestions questions;
    
    /**
     * 评估结果
     */
    private InterviewEvaluation evaluation;

    /**
     * 当前流程状态：ANALYZED / QUESTIONS_READY / EVALUATED
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最近更新时间
     */
    private LocalDateTime updatedAt;
}
