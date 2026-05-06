package com.h.resumeagent.controller;

import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.entity.*;
import com.h.resumeagent.common.repository.*;
import com.h.resumeagent.service.AuthService;
import com.h.resumeagent.service.ForumService;
import com.h.resumeagent.service.MockInterviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Transactional(readOnly = true)
public class AdminController {

    private final AuthService authService;
    private final MockInterviewService interviewService;
    private final ResumeHistoryViewRepository resumeHistoryViewRepository;
    private final ResumeSessionRepository resumeSessionRepository;
    private final UserRepository userRepository;
    private final ForumService forumService;
    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final ForumCategoryRepository categoryRepository;
    private final ForumEssenceRepository essenceRepository;

    public AdminController(AuthService authService, MockInterviewService interviewService,
            ResumeHistoryViewRepository resumeHistoryViewRepository,
            ResumeSessionRepository resumeSessionRepository,
            UserRepository userRepository,
            ForumService forumService,
            ForumPostRepository forumPostRepository,
            ForumCommentRepository forumCommentRepository,
            ForumCategoryRepository categoryRepository,
            ForumEssenceRepository essenceRepository) {
        this.authService = authService;
        this.interviewService = interviewService;
        this.resumeHistoryViewRepository = resumeHistoryViewRepository;
        this.resumeSessionRepository = resumeSessionRepository;
        this.userRepository = userRepository;
        this.forumService = forumService;
        this.forumPostRepository = forumPostRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.categoryRepository = categoryRepository;
        this.essenceRepository = essenceRepository;
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<UserEntity> users = userRepository.findAll();
        List<Map<String, Object>> userViews = new ArrayList<>();
        for (UserEntity user : users) {
            Map<String, Object> userView = new HashMap<>();
            userView.put("id", user.getId());
            userView.put("username", user.getUsername());
            userView.put("email", user.getEmail());
            userView.put("displayName", user.getDisplayName());
            userView.put("role", user.getRole());
            userView.put("status", user.getStatus());
            userView.put("createdAt", user.getCreatedAt());
            userViews.add(userView);
        }
        return ResponseEntity.ok(userViews);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return authService.findUserById(id)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "displayName", user.getDisplayName(),
                        "role", user.getRole(),
                        "status", user.getStatus(),
                        "createdAt", user.getCreatedAt())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            UserEntity user = authService.updateUser(
                    id,
                    request.get("email"),
                    request.get("displayName"));
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "displayName", user.getDisplayName(),
                    "role", user.getRole(),
                    "status", user.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer status = request.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
            }
            authService.updateUserStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "User status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新用户密码
     */
    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            if (password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "password is required"));
            }
            authService.updateUserPassword(id, password);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalUsers = userRepository.count();
        long evaluatedCount = resumeHistoryViewRepository.countByStatus(ResumeStatus.EVALUATED);
        long totalResumes = resumeHistoryViewRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalResumes", totalResumes,
                "totalInterviews", evaluatedCount,
                "activeUsers", totalUsers));
    }

    /**
     * 获取最近活动
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities(@RequestParam(defaultValue = "10") int limit) {
        List<ResumeHistoryViewEntity> recent = resumeHistoryViewRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, limit));
        List<Map<String, Object>> activities = new ArrayList<>();
        for (ResumeHistoryViewEntity item : recent) {
            String icon = switch (item.getStatus()) {
                case ANALYZED -> "📄";
                case QUESTIONS_READY -> "🎯";
                case EVALUATED -> "✅";
                default -> "📋";
            };
            String userName = getUserDisplayName(item.getUserId());
            String text;
            if (item.getStatus() == ResumeStatus.EVALUATED) {
                text = String.format("用户「%s」完成了面试评估", userName);
            } else if (item.getStatus() == ResumeStatus.QUESTIONS_READY) {
                text = String.format("用户「%s」生成了面试问题", userName);
            } else {
                text = String.format("用户「%s」上传了简历", userName);
            }
            activities.add(Map.of(
                    "id", activities.size() + 1,
                    "icon", icon,
                    "text", text,
                    "time", item.getUpdatedAt() != null ? item.getUpdatedAt().toString() : item.getCreatedAt().toString()
            ));
        }
        return ResponseEntity.ok(activities);
    }

    /**
     * 查看所有用户的简历分析历史
     */
    @GetMapping("/resume-history")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllResumeHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ResumeSessionEntity> sessions = resumeSessionRepository.findAllByOrderByUpdatedAtDesc(pageable);
        List<Map<String, Object>> content = sessions.stream().map(session -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", session.getId());
            item.put("resumeId", session.getResumeId());
            item.put("userId", session.getUserId());
            item.put("status", session.getStatus());
            item.put("positionType", session.getPositionType());
            item.put("resumeOverallScore", session.getResumeOverallScore());
            item.put("evaluationOverallScore", session.getEvaluationOverallScore());
            item.put("questionCount", countInterviewQuestions(session));
            item.put("createdAt", session.getCreatedAt());
            item.put("updatedAt", session.getUpdatedAt());
            return item;
        }).collect(java.util.stream.Collectors.toList());
        long total = resumeSessionRepository.count();
        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size),
                "currentPage", page,
                "pageSize", size));
    }

    /**
     * 导出简历分析数据
     */
    @GetMapping("/resume-history/export")
    @Transactional(readOnly = true)
    public ResponseEntity<?> exportResumeHistory() {
        List<ResumeSessionEntity> sessions = resumeSessionRepository.findAll();
        Map<Long, UserEntity> userMap = userRepository.findAllById(
                sessions.stream()
                        .map(ResumeSessionEntity::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("会话ID,简历ID,用户ID,用户名,显示名称,邮箱,岗位类型,状态,简历评分,面试评分,问题数量,简历摘要,简历文本,创建时间,更新时间\n");
        for (ResumeSessionEntity session : sessions) {
            UserEntity user = session.getUserId() == null ? null : userMap.get(session.getUserId());
            csv.append(csvRow(
                    session.getId(),
                    session.getResumeId(),
                    session.getUserId(),
                    user == null ? null : user.getUsername(),
                    user == null ? null : user.getDisplayName(),
                    user == null ? null : user.getEmail(),
                    toPositionTypeLabel(session.getPositionType()),
                    toStatusLabel(session.getStatus()),
                    session.getResumeOverallScore(),
                    session.getEvaluationOverallScore(),
                    countInterviewQuestions(session),
                    session.getResumeSummary(),
                    session.getResumeText(),
                    session.getCreatedAt(),
                    session.getUpdatedAt()));
        }
        return ResponseEntity.ok()
.header("Content-Type", "text/csv;charset=UTF-8")
                .header("Content-Disposition", "attachment;filename=resume-history.csv")
                .body(csv.toString());
    }

    /**
     * 获取所有用户的简历列表（管理员）
     */
    @GetMapping("/resumes")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllResumes(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        List<ResumeSessionEntity> allSessions = resumeSessionRepository.findAllByOrderByUpdatedAtDesc(pageable);

        Map<Long, UserEntity> userMap = userRepository.findAllById(
                allSessions.stream()
                        .map(ResumeSessionEntity::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        List<Map<String, Object>> content = allSessions.stream().map(session -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", session.getId());
            item.put("resumeId", session.getResumeId());
            item.put("userId", session.getUserId());
            UserEntity user = session.getUserId() == null ? null : userMap.get(session.getUserId());
            item.put("username", user == null ? null : user.getUsername());
            item.put("displayName", user == null ? null : user.getDisplayName());
            item.put("email", user == null ? null : user.getEmail());
            item.put("status", session.getStatus());
            item.put("positionType", session.getPositionType());
            item.put("resumeOverallScore", session.getResumeOverallScore());
            item.put("evaluationOverallScore", session.getEvaluationOverallScore());
            item.put("questionCount", countInterviewQuestions(session));
            item.put("resumeText", session.getResumeText());
            item.put("createdAt", session.getCreatedAt());
            item.put("updatedAt", session.getUpdatedAt());

            if (keyword != null && !keyword.isBlank()) {
                String username = user == null ? "" : (user.getUsername() != null ? user.getUsername() : "");
                String displayName = user == null ? "" : (user.getDisplayName() != null ? user.getDisplayName() : "");
                String resumeText = session.getResumeText() != null ? session.getResumeText() : "";
                if (!username.contains(keyword) && !displayName.contains(keyword) && !resumeText.contains(keyword)) {
                    return null;
                }
            }
            return item;
        }).filter(Objects::nonNull).collect(java.util.stream.Collectors.toList());

        long total = resumeSessionRepository.count();
        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size),
                "currentPage", page,
                "pageSize", size));
    }

    /**
     * 导出所有简历数据（管理员）
     */
    @GetMapping("/resumes/export")
    @Transactional(readOnly = true)
    public ResponseEntity<?> exportResumes() {
        List<ResumeSessionEntity> sessions = resumeSessionRepository.findAll();
        Map<Long, UserEntity> userMap = userRepository.findAllById(
                sessions.stream()
                        .map(ResumeSessionEntity::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("会话ID,简历ID,用户ID,用户名,显示名称,邮箱,岗位类型,状态,简历评分,面试评分,问题数量,简历摘要,简历文本,创建时间,更新时间\n");
        for (ResumeSessionEntity session : sessions) {
            UserEntity user = session.getUserId() == null ? null : userMap.get(session.getUserId());
            csv.append(csvRow(
                    session.getId(),
                    session.getResumeId(),
                    session.getUserId(),
                    user == null ? null : user.getUsername(),
                    user == null ? null : user.getDisplayName(),
                    user == null ? null : user.getEmail(),
                    toPositionTypeLabel(session.getPositionType()),
                    toStatusLabel(session.getStatus()),
                    session.getResumeOverallScore(),
                    session.getEvaluationOverallScore(),
                    countInterviewQuestions(session),
                    session.getResumeSummary(),
                    session.getResumeText(),
                    session.getCreatedAt(),
                    session.getUpdatedAt()));
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv;charset=UTF-8")
                .header("Content-Disposition", "attachment;filename=resumes.csv")
                .body(csv.toString());
    }

    private String nullToEmpty(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private String csvRow(Object... fields) {
        return Arrays.stream(fields).map(this::csvEscape).collect(Collectors.joining(",")) + "\n";
    }

    private String csvEscape(Object value) {
        String text = nullToEmpty(value);
        boolean needQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        String escaped = text.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
    }

    private String toPositionTypeLabel(String positionType) {
        return nullToEmpty(interviewService.displayPositionType(positionType));
    }

    private String toStatusLabel(ResumeStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case ANALYZED -> "已分析";
            case QUESTIONS_READY -> "已生成问题";
            case EVALUATED -> "已评估";
        };
    }

    private long countInterviewQuestions(ResumeSessionEntity session) {
        if (session == null || session.getId() == null) {
            return 0L;
        }
        return resumeSessionRepository.countInterviewQuestionsBySessionId(session.getId());
    }

    /**
     * 查看所有用户的面试历史
     */
    @GetMapping("/interview-history")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllInterviewHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ResumeSessionEntity> sessions = resumeSessionRepository.findAllByOrderByUpdatedAtDesc(pageable);
        List<Map<String, Object>> content = sessions.stream().map(session -> {
            Map<String, Object> item = new HashMap<>();
            item.put("resumeId", session.getResumeId());
            item.put("userId", session.getUserId());
            item.put("status", session.getStatus());
            item.put("positionType", session.getPositionType());
            item.put("resumeOverallScore", session.getResumeOverallScore());
            item.put("evaluationOverallScore", session.getEvaluationOverallScore());
            item.put("questionCount", countInterviewQuestions(session));
            item.put("createdAt", session.getCreatedAt());
            item.put("updatedAt", session.getUpdatedAt());
            return item;
        }).collect(java.util.stream.Collectors.toList());
        long total = resumeSessionRepository.count();
        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size),
                "currentPage", page,
                "pageSize", size));
    }

    /**
     * 获取面试详情
*/
    @GetMapping("/interview-history/{resumeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getInterviewDetail(@PathVariable String resumeId) {
        ResumeData resumeData = interviewService.getResumeById(resumeId);
        if (resumeData == null) {
            return ResponseEntity.notFound().build();
        }

        return resumeSessionRepository.findByResumeId(resumeId)
                .map(session -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("resumeId", session.getResumeId());
                    detail.put("userId", session.getUserId());
                    detail.put("status", session.getStatus());
                    detail.put("positionType", session.getPositionType());
                    detail.put("resumeOverallScore", session.getResumeOverallScore());
                    detail.put("evaluationOverallScore", session.getEvaluationOverallScore());
                    detail.put("questionCount", countInterviewQuestions(session));
                    detail.put("createdAt", session.getCreatedAt());
                    detail.put("updatedAt", session.getUpdatedAt());
                    detail.put("questions", resumeData.getQuestions());
                    detail.put("evaluation", resumeData.getEvaluation());
                    return ResponseEntity.ok(detail);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取简历详情（管理员）
     */
    @GetMapping("/resume-history/{resumeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getResumeDetail(@PathVariable String resumeId) {
        ResumeData resumeData = interviewService.getResumeById(resumeId);
        if (resumeData == null) {
            return ResponseEntity.notFound().build();
        }

        return resumeSessionRepository.findByResumeId(resumeId)
                .map(session -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("resumeId", session.getResumeId());
                    detail.put("userId", session.getUserId());
                    detail.put("status", session.getStatus());
                    detail.put("positionType", session.getPositionType());
                    detail.put("resumeOverallScore", session.getResumeOverallScore());
                    detail.put("evaluationOverallScore", session.getEvaluationOverallScore());
                    detail.put("questionCount", countInterviewQuestions(session));
                    detail.put("createdAt", session.getCreatedAt());
                    detail.put("updatedAt", session.getUpdatedAt());
                    detail.put("resumeText", resumeData.getResumeText());
                    detail.put("scoreResult", resumeData.getScoreResult());
                    return ResponseEntity.ok(detail);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 管理角色和权限
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(Map.of(
                "roles", List.of(
                        Map.of("name", "ADMIN", "description", "管理员角色", "permissions", List.of("MANAGE_USERS", "MANAGE_SYSTEM", "VIEW_ALL_DATA")),
                        Map.of("name", "USER", "description", "普通用户角色", "permissions", List.of("VIEW_OWN_DATA", "UPLOAD_RESUME", "TAKE_INTERVIEW")))));
    }

    /**
     * 分配管理员权限
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            if (role == null || (!role.equals("ADMIN") && !role.equals("USER"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }
            authService.updateUserRole(id, role);
            return ResponseEntity.ok(Map.of(
                    "message", "用户角色更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取所有帖子列表（管理员）
     */
    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPostEntity> posts;

        if ("essence".equals(filter) && keyword != null && !keyword.isBlank()) {
            posts = forumPostRepository.findByIsEssenceAndTitleContainingIgnoreCase(true, keyword, pageable);
        } else if ("essence".equals(filter)) {
            posts = forumPostRepository.findByIsEssenceOrderByCreatedAtDesc(true, pageable);
        } else if ("top".equals(filter) && keyword != null && !keyword.isBlank()) {
            posts = forumPostRepository.findByIsEssenceAndTitleContainingIgnoreCase(true, keyword, pageable);
        } else if ("top".equals(filter)) {
            posts = forumPostRepository.findByIsTopOrderByCreatedAtDesc(true, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            posts = forumPostRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            posts = forumPostRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<Map<String, Object>> content = posts.getContent().stream().map(post -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", post.getId());
            item.put("title", post.getTitle());
            item.put("content", post.getContent());
            item.put("authorId", post.getAuthorId());
            item.put("authorName", getUserDisplayName(post.getAuthorId()));
            item.put("categoryId", post.getCategoryId());
            item.put("categoryName", post.getCategory() != null ? post.getCategory().getName() : null);
            item.put("isEssence", post.getIsEssence());
            item.put("isTop", post.getIsTop());
            item.put("viewCount", post.getViewCount());
            item.put("likeCount", post.getLikeCount());
            item.put("dislikeCount", post.getDislikeCount());
            item.put("commentCount", post.getCommentCount());
            item.put("createdAt", post.getCreatedAt());
            item.put("updatedAt", post.getUpdatedAt());
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", posts.getTotalElements(),
                "totalPages", posts.getTotalPages(),
                "currentPage", page,
                "pageSize", size));
    }

    /**
     * 获取帖子详情（管理员）
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long id) {
        return forumPostRepository.findById(id)
                .map(post -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("id", post.getId());
                    detail.put("title", post.getTitle());
                    detail.put("content", post.getContent());
                    detail.put("authorId", post.getAuthorId());
                    detail.put("authorName", getUserDisplayName(post.getAuthorId()));
                    detail.put("categoryId", post.getCategoryId());
                    detail.put("categoryName", post.getCategory() != null ? post.getCategory().getName() : null);
                    detail.put("isEssence", post.getIsEssence());
                    detail.put("isTop", post.getIsTop());
                    detail.put("viewCount", post.getViewCount());
                    detail.put("likeCount", post.getLikeCount());
                    detail.put("dislikeCount", post.getDislikeCount());
                    detail.put("commentCount", post.getCommentCount());
                    detail.put("createdAt", post.getCreatedAt());
                    detail.put("updatedAt", post.getUpdatedAt());
                    return ResponseEntity.ok(detail);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除帖子（管理员）
     */
    @DeleteMapping("/posts/{id}")
    @Transactional
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            ForumPostEntity post = forumPostRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            if (post.getCategoryId() != null) {
                categoryRepository.decrementPostCount(post.getCategoryId());
            }
            forumPostRepository.delete(post);
            return ResponseEntity.ok(Map.of("message", "帖子删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 设置帖子为精华（管理员）
     */
    @PostMapping("/posts/{id}/essence")
    @Transactional
    public ResponseEntity<?> setEssence(@PathVariable Long id) {
        try {
            if (!forumPostRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            forumPostRepository.updateEssence(id, true);
            return ResponseEntity.ok(Map.of("message", "设置精华成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 取消精华（管理员）
     */
    @DeleteMapping("/posts/{id}/essence")
    @Transactional
    public ResponseEntity<?> removeEssence(@PathVariable Long id) {
        try {
            if (!forumPostRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            forumPostRepository.updateEssence(id, false);
            return ResponseEntity.ok(Map.of("message", "取消精华成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 置顶帖子（管理员）
     */
    @PostMapping("/posts/{id}/top")
    @Transactional
    public ResponseEntity<?> setTop(@PathVariable Long id) {
        try {
            if (!forumPostRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            forumPostRepository.updateTop(id, true);
            return ResponseEntity.ok(Map.of("message", "置顶成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 取消置顶（管理员）
     */
    @DeleteMapping("/posts/{id}/top")
    @Transactional
    public ResponseEntity<?> removeTop(@PathVariable Long id) {
        try {
            if (!forumPostRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            forumPostRepository.updateTop(id, false);
            return ResponseEntity.ok(Map.of("message", "取消置顶成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取帖子所有评论（管理员）
     */
    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<?> getPostComments(@PathVariable Long id) {
        List<ForumCommentEntity> comments = forumCommentRepository.findByPostIdOrderByCreatedAtAsc(id);
        List<Map<String, Object>> content = comments.stream().map(comment -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId());
            item.put("postId", comment.getPostId());
            item.put("parentId", comment.getParentId());
            item.put("authorId", comment.getAuthorId());
            item.put("authorName", getUserDisplayName(comment.getAuthorId()));
            item.put("content", comment.getContent());
            item.put("likeCount", comment.getLikeCount());
            item.put("dislikeCount", comment.getDislikeCount());
            item.put("createdAt", comment.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(content);
    }

    /**
     * 删除评论（管理员）
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            if (!forumCommentRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            forumCommentRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "评论删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取论坛统计数据
     */
    @GetMapping("/forum-stats")
    public ResponseEntity<?> getForumStats() {
        long totalPosts = forumPostRepository.count();
        long totalComments = forumCommentRepository.count();
        long essenceCount = forumPostRepository.countByIsEssence(true);
        long topCount = forumPostRepository.countByIsTop(true);
        return ResponseEntity.ok(Map.of(
                "totalPosts", totalPosts,
                "totalComments", totalComments,
                "essenceCount", essenceCount,
                "topCount", topCount));
    }

    /**
     * 获取所有精华帖列表（管理员）
     */
    @GetMapping("/essences")
    public ResponseEntity<?> getAllEssences(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPostEntity> posts;

        if (keyword != null && !keyword.isBlank()) {
            posts = forumPostRepository.findByIsEssenceAndTitleContainingIgnoreCase(true, keyword, pageable);
        } else {
            posts = forumPostRepository.findByIsEssenceOrderByCreatedAtDesc(true, pageable);
        }

        List<Map<String, Object>> content = posts.getContent().stream().map(post -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", post.getId());
            item.put("title", post.getTitle());
            item.put("content", post.getContent());
            item.put("authorId", post.getAuthorId());
            item.put("authorName", getUserDisplayName(post.getAuthorId()));
            item.put("categoryId", post.getCategoryId());
            item.put("categoryName", post.getCategory() != null ? post.getCategory().getName() : null);
            item.put("isEssence", post.getIsEssence());
            item.put("isTop", post.getIsTop());
            item.put("viewCount", post.getViewCount());
            item.put("likeCount", post.getLikeCount());
            item.put("commentCount", post.getCommentCount());
            item.put("createdAt", post.getCreatedAt());
            item.put("updatedAt", post.getUpdatedAt());
            item.put("essenceTime", getEssenceTime(post.getId()));
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", posts.getTotalElements(),
                "totalPages", posts.getTotalPages(),
                "currentPage", page,
                "pageSize", size));
    }

    /**
     * 获取精华帖统计数据
     */
    @GetMapping("/essence-stats")
    public ResponseEntity<?> getEssenceStats() {
        long totalEssences = forumPostRepository.countByIsEssence(true);
        long totalAuthors = forumPostRepository.findDistinctEssenceAuthors().size();
        long totalViews = forumPostRepository.sumViewsByEssence();
        long totalLikes = forumPostRepository.sumLikesByEssence();
        return ResponseEntity.ok(Map.of(
                "totalEssences", totalEssences,
                "totalAuthors", totalAuthors,
                "totalViews", totalViews,
                "totalLikes", totalLikes));
    }

    /**
     * 获取热门作者列表（管理员）
     * 基于发帖数和点赞数自动计算热门作者
     */
    @GetMapping("/hot-authors")
    public ResponseEntity<?> getHotAuthors(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        List<Object[]> hotAuthorsData = forumPostRepository.findHotAuthorsWithStats(100);

        List<Map<String, Object>> authorList = new ArrayList<>();
        for (Object[] row : hotAuthorsData) {
            Long authorId = ((Number) row[0]).longValue();
            Integer postCount = ((Number) row[1]).intValue();
            Integer likeCount = row.length > 2 && row[2] != null ? ((Number) row[2]).intValue() : 0;

            UserEntity user = userRepository.findById(authorId).orElse(null);
            if (user == null) continue;

            if ("ADMIN".equals(user.getRole())) {
                continue;
            }

            if (keyword != null && !keyword.isBlank()) {
                String username = user.getUsername() != null ? user.getUsername() : "";
                String displayName = user.getDisplayName() != null ? user.getDisplayName() : "";
                if (!username.contains(keyword) && !displayName.contains(keyword)) {
                    continue;
                }
            }

            Map<String, Object> authorMap = new HashMap<>();
            authorMap.put("id", user.getId());
            authorMap.put("username", user.getUsername());
            authorMap.put("email", user.getEmail());
            authorMap.put("displayName", user.getDisplayName());
            authorMap.put("postCount", postCount);
            authorMap.put("likeCount", likeCount);
            authorMap.put("createdAt", user.getCreatedAt());
            authorList.add(authorMap);
        }

        authorList.sort((a, b) -> {
            Integer likesA = (Integer) a.get("likeCount");
            Integer likesB = (Integer) b.get("likeCount");
            return likesB.compareTo(likesA);
        });

        int start = page * size;
        int end = Math.min(start + size, authorList.size());
        List<Map<String, Object>> pagedContent = start < authorList.size() ? authorList.subList(start, end) : Collections.emptyList();

        return ResponseEntity.ok(Map.of(
                "content", pagedContent,
                "totalElements", authorList.size(),
                "totalPages", (int) Math.ceil((double) authorList.size() / size),
                "currentPage", page,
                "pageSize", size));
    }

    /**
     * 获取作者统计数据
     */
    @GetMapping("/author-stats")
    public ResponseEntity<?> getAuthorStats() {
        List<UserEntity> users = userRepository.findAll();
        long totalAuthors = users.size();
        List<Object[]> hotAuthorsData = forumPostRepository.findHotAuthorsWithStats(100);
        long hotAuthors = hotAuthorsData.size();
        long totalPosts = forumPostRepository.count();
        long totalLikes = forumPostRepository.sumLikes();
        return ResponseEntity.ok(Map.of(
                "totalAuthors", totalAuthors,
                "hotAuthors", hotAuthors,
                "totalPosts", totalPosts,
                "totalLikes", totalLikes));
    }

    /**
     * 获取作者的帖子列表（管理员）
     */
    @GetMapping("/authors/{id}/posts")
    public ResponseEntity<?> getAuthorPosts(@PathVariable Long id) {
        List<ForumPostEntity> posts = forumPostRepository.findByAuthorId(id);
        List<Map<String, Object>> content = posts.stream().map(post -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", post.getId());
            item.put("title", post.getTitle());
            item.put("content", post.getContent());
            item.put("authorId", post.getAuthorId());
            item.put("authorName", getUserDisplayName(post.getAuthorId()));
            item.put("categoryId", post.getCategoryId());
            item.put("categoryName", post.getCategory() != null ? post.getCategory().getName() : null);
            item.put("isEssence", post.getIsEssence());
            item.put("isTop", post.getIsTop());
            item.put("viewCount", post.getViewCount());
            item.put("likeCount", post.getLikeCount());
            item.put("commentCount", post.getCommentCount());
            item.put("createdAt", post.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(content);
    }

    private String getUserDisplayName(Long userId) {
        if (userId == null) return "Unknown";
        return userRepository.findById(userId)
                .map(u -> u.getDisplayName() != null ? u.getDisplayName() : u.getUsername())
                .orElse("Unknown");
    }

    private LocalDateTime getEssenceTime(Long postId) {
        if (postId == null) return null;
        return essenceRepository.findByPostId(postId)
                .map(ForumEssenceEntity::getCreatedAt)
                .orElse(null);
    }
}
