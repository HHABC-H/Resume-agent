package com.h.resumeagent.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Integer postCount;
}
