package com.h.resumeagent.mapper;

import com.h.resumeagent.common.dto.ResumeHistoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResumeHistoryMapper {

    List<ResumeHistoryItem> selectRecentHistory(@Param("offset") int offset, @Param("limit") int limit);

    List<ResumeHistoryItem> selectRecentHistoryByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    long countRecentHistory();

    long countRecentHistoryByUserId(@Param("userId") Long userId);
}
