package com.h.resumeagent.controller;

import com.h.resumeagent.common.dto.ApiResponse;
import com.h.resumeagent.common.dto.ResumeEditDTO;
import com.h.resumeagent.common.dto.SaveResumeRequest;
import com.h.resumeagent.interceptor.AuthTokenInterceptor;
import com.h.resumeagent.service.ResumeEditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resume/edit")
public class ResumeEditController {

    private final ResumeEditService resumeEditService;

    public ResumeEditController(ResumeEditService resumeEditService) {
        this.resumeEditService = resumeEditService;
    }

    @PostMapping("/draft")
    public ApiResponse<ResumeEditDTO> saveDraft(@RequestBody SaveResumeRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        ResumeEditDTO dto = resumeEditService.saveDraft(userId, request);
        return ApiResponse.success("草稿保存成功", dto);
    }

    @PutMapping("/draft")
    public ApiResponse<ResumeEditDTO> updateDraft(@RequestBody SaveResumeRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        ResumeEditDTO dto = resumeEditService.updateDraft(userId, request);
        return ApiResponse.success("草稿更新成功", dto);
    }

    @GetMapping("/draft")
    public ApiResponse<ResumeEditDTO> getCurrentDraft(HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        ResumeEditDTO dto = resumeEditService.getCurrentDraft(userId);
        return ApiResponse.success(dto);
    }

    @PostMapping("/save")
    public ApiResponse<ResumeEditDTO> saveAsVersion(@RequestBody SaveResumeRequest request, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        ResumeEditDTO dto = resumeEditService.saveAsVersion(userId, request);
        return ApiResponse.success("简历保存成功", dto);
    }

    @GetMapping("/history")
    public ApiResponse<Page<ResumeEditDTO>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<ResumeEditDTO> result = resumeEditService.getHistory(userId, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/history/{id}")
    public ApiResponse<ResumeEditDTO> getHistoryDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        try {
            ResumeEditDTO dto = resumeEditService.getHistoryDetail(id, userId);
            return ApiResponse.success(dto);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/history/{id}")
    public ApiResponse<Void> deleteHistory(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "未授权，请先登录");
        }
        try {
            resumeEditService.deleteHistory(id, userId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute(AuthTokenInterceptor.CURRENT_USER_ID_ATTR);
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }
}