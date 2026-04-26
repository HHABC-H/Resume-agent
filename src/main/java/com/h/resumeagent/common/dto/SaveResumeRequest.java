package com.h.resumeagent.common.dto;

import java.util.Map;

public class SaveResumeRequest {
    private Map<String, Object> structuredData;
    private String resumeText;
    private boolean publish;

    public Map<String, Object> getStructuredData() { return structuredData; }
    public void setStructuredData(Map<String, Object> structuredData) { this.structuredData = structuredData; }
    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }
    public boolean isPublish() { return publish; }
    public void setPublish(boolean publish) { this.publish = publish; }
}