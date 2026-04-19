package com.h.resumeagent.service;

import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeScoreResult;

import java.io.IOException;

public interface ResumeService {

    ResumeScoreResult scoreResume(String resumeText) throws IOException;
    
    ResumeScoreResult scoreResume(String resumeText, String positionType) throws IOException;

    void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult);

    void saveResume(String resumeId, String resumeText, ResumeScoreResult scoreResult, Long userId);

    void saveResume(
            String resumeId,
            String resumeText,
            ResumeScoreResult scoreResult,
            Long userId,
            String positionType);

    ResumeData getResumeById(String resumeId);

    ResumeData getResumeById(String resumeId, Long userId);
}
