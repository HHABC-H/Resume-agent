package com.h.resumeagent.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatePostRequest {
    private String title;

    private String content;

    private Long categoryId;

    private List<Long> tagIds;
}
