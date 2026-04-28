package com.h.resumeagent.service;

import com.h.resumeagent.common.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ForumService {
    Page<ForumPostDTO> getPosts(Pageable pageable);
    Page<ForumPostDTO> getPostsByCategory(Long categoryId, Pageable pageable);
    Page<ForumPostDTO> getPostsByAuthor(Long authorId, Pageable pageable);
    Page<ForumPostDTO> getEssences(Pageable pageable);
    Page<ForumPostDTO> getEssencesSince(java.time.LocalDateTime startTime, Pageable pageable);
    ForumPostDetailDTO getPostDetail(Long postId, Long userId);
    ForumPostDTO createPost(CreatePostRequest request, Long authorId);
    void deletePost(Long postId, Long userId);
    void incrementViewCount(Long postId);

    ForumCommentDTO createComment(Long postId, Long parentId, String content, Long authorId);
    List<ForumCommentDTO> getComments(Long postId);

    void likePost(Long postId, Long userId);
    void dislikePost(Long postId, Long userId);
    void unlikePost(Long postId, Long userId);
    void likeComment(Long commentId);

    void dislikeComment(Long commentId);

    void unlikeComment(Long commentId);

    void undislikeComment(Long commentId);
    void setEssence(Long postId, Long operatorId);
    void removeEssence(Long postId);
    void setTop(Long postId, Long operatorId);
    void removeTop(Long postId);

    List<ForumCategoryDTO> getCategories();
    Page<ForumPostDTO> getHotPosts(Pageable pageable);
    List<HotAuthorDTO> getHotAuthors(int limit);
    List<HotAuthorDTO> getHotAuthorsSince(java.time.LocalDateTime startTime, int limit);

    void toggleBookmark(Long postId, Long userId);
    Page<ForumPostDTO> getBookmarks(Long userId, Pageable pageable);
    boolean isBookmarked(Long postId, Long userId);
}
