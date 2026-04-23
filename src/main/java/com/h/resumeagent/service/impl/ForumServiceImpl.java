package com.h.resumeagent.service.impl;

import com.h.resumeagent.common.dto.*;
import com.h.resumeagent.common.entity.*;
import com.h.resumeagent.common.repository.*;
import com.h.resumeagent.service.ForumService;
import com.h.resumeagent.common.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForumServiceImpl implements ForumService {

    private final ForumPostRepository postRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumCategoryRepository categoryRepository;
    private final ForumTagRepository tagRepository;
    private final ForumEssenceRepository essenceRepository;
    private final UserRepository userRepository;

    public ForumServiceImpl(
            ForumPostRepository postRepository,
            ForumCommentRepository commentRepository,
            ForumCategoryRepository categoryRepository,
            ForumTagRepository tagRepository,
            ForumEssenceRepository essenceRepository,
            UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.essenceRepository = essenceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<ForumPostDTO> getPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toPostDTO);
    }

    @Override
    public Page<ForumPostDTO> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable)
                .map(this::toPostDTO);
    }

    @Override
    public Page<ForumPostDTO> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable)
                .map(this::toPostDTO);
    }

    @Override
    public Page<ForumPostDTO> getEssences(Pageable pageable) {
        return postRepository.findEssences(pageable)
                .map(this::toPostDTO);
    }

    @Override
    public ForumPostDetailDTO getPostDetail(Long postId, Long userId) {
        ForumPostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        List<ForumCommentDTO> comments = getComments(postId);

        return ForumPostDetailDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthorId())
                .authorName(getUserDisplayName(post.getAuthorId()))
                .categoryId(post.getCategoryId())
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .commentCount(post.getCommentCount())
                .status(post.getStatus())
                .tags(getPostTags(post.getId()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(comments)
                .build();
    }

    @Override
    public ForumPostDTO createPost(CreatePostRequest request, Long authorId) {
        ForumPostEntity post = ForumPostEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(authorId)
                .categoryId(request.getCategoryId())
                .build();

        post = postRepository.save(post);

        if (request.getCategoryId() != null) {
            categoryRepository.incrementPostCount(request.getCategoryId());
        }

        return toPostDTO(post);
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        ForumPostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }

        if (post.getCategoryId() != null) {
            categoryRepository.decrementPostCount(post.getCategoryId());
        }

        postRepository.delete(post);
    }

    @Override
    public void incrementViewCount(Long postId) {
        postRepository.incrementViewCount(postId);
    }

    @Override
    public ForumCommentDTO createComment(Long postId, Long parentId, String content, Long authorId) {
        ForumCommentEntity comment = ForumCommentEntity.builder()
                .postId(postId)
                .parentId(parentId != null ? parentId : 0L)
                .authorId(authorId)
                .content(content)
                .build();

        comment = commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        return toCommentDTO(comment);
    }

    @Override
    public List<ForumCommentDTO> getComments(Long postId) {
        List<ForumCommentEntity> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        Map<Long, List<ForumCommentDTO>> parentMap = comments.stream()
                .map(this::toCommentDTO)
                .collect(Collectors.groupingBy(ForumCommentDTO::getParentId));

        List<ForumCommentDTO> rootComments = parentMap.getOrDefault(0L, Collections.emptyList());

        for (ForumCommentDTO comment : rootComments) {
            comment.setChildren(parentMap.getOrDefault(comment.getId(), Collections.emptyList()));
        }

        return rootComments;
    }

    @Override
    public void likePost(Long postId) {
        postRepository.incrementLikeCount(postId);
    }

    @Override
    public void dislikePost(Long postId) {
        postRepository.incrementDislikeCount(postId);
    }

    @Override
    public void likeComment(Long commentId) {
        commentRepository.incrementLikeCount(commentId);
    }

    @Override
    public void dislikeComment(Long commentId) {
        commentRepository.incrementDislikeCount(commentId);
    }

    @Override
    public void setEssence(Long postId, Long operatorId) {
        if (!essenceRepository.existsByPostId(postId)) {
            ForumEssenceEntity essence = ForumEssenceEntity.builder()
                    .postId(postId)
                    .operatorId(operatorId)
                    .build();
            essenceRepository.save(essence);
        }
        postRepository.updateStatus(postId, 1);
    }

    @Override
    public void removeEssence(Long postId) {
        essenceRepository.findByPostId(postId).ifPresent(essenceRepository::delete);
        postRepository.updateStatus(postId, 0);
    }

    @Override
    public void setTop(Long postId, Long operatorId) {
        postRepository.updateStatus(postId, 2);
    }

    @Override
    public void removeTop(Long postId) {
        postRepository.updateStatus(postId, 0);
    }

    @Override
    public List<ForumCategoryDTO> getCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> ForumCategoryDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .postCount(c.getPostCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Page<ForumPostDTO> getHotPosts(Pageable pageable) {
        return postRepository.findByOrderByViewCountDesc(pageable)
                .map(this::toPostDTO);
    }

    @Override
    public List<HotAuthorDTO> getHotAuthors(int limit) {
        List<Object[]> results = postRepository.findHotAuthors(limit);
        List<HotAuthorDTO> authors = new java.util.ArrayList<>();
        for (Object[] row : results) {
            Long authorId = ((Number) row[0]).longValue();
            Integer postCount = ((Number) row[1]).intValue();
            String username = userRepository.findById(authorId)
                    .map(u -> u.getUsername())
                    .orElse("Unknown");
            String displayName = userRepository.findById(authorId)
                    .map(u -> u.getDisplayName())
                    .orElse(username);
            authors.add(HotAuthorDTO.builder()
                    .id(authorId)
                    .username(username)
                    .displayName(displayName)
                    .postCount(postCount)
                    .build());
        }
        return authors;
    }

    private ForumPostDTO toPostDTO(ForumPostEntity post) {
        String contentPreview = post.getContent();
        if (contentPreview != null && contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }

        return ForumPostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .contentPreview(contentPreview)
                .authorId(post.getAuthorId())
                .authorName(getUserDisplayName(post.getAuthorId()))
                .categoryId(post.getCategoryId())
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .commentCount(post.getCommentCount())
                .status(post.getStatus())
                .tags(getPostTags(post.getId()))
                .createdAt(post.getCreatedAt())
                .build();
    }

    private ForumCommentDTO toCommentDTO(ForumCommentEntity comment) {
        return ForumCommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .authorId(comment.getAuthorId())
                .authorName(getUserDisplayName(comment.getAuthorId()))
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private String getUserDisplayName(Long userId) {
        if (userId == null) return "Unknown";
        return userRepository.findById(userId)
                .map(u -> u.getDisplayName() != null ? u.getDisplayName() : u.getUsername())
                .orElse("Unknown");
    }

    private List<String> getPostTags(Long postId) {
        return Collections.emptyList();
    }
}
