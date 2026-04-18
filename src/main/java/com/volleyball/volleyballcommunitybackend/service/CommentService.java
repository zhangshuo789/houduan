package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.CommentRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.CommentResponse;
import com.volleyball.volleyballcommunitybackend.entity.Comment;
import com.volleyball.volleyballcommunitybackend.entity.Post;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.CommentRepository;
import com.volleyball.volleyballcommunitybackend.repository.PostRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import com.volleyball.volleyballcommunitybackend.util.SensitiveWordFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FileService fileService;
    private final SensitiveWordFilter sensitiveWordFilter;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository,
                        PostRepository postRepository, FileService fileService,
                        SensitiveWordFilter sensitiveWordFilter) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.fileService = fileService;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    public CommentResponse addComment(Long postId, CommentRequest request, Long userId, HttpServletRequest httpRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            if (!parent.getPostId().equals(postId)) {
                throw new RuntimeException("父评论不属于该帖子");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Comment comment = new Comment();
        comment.setContent(sensitiveWordFilter.filter(request.getContent()));
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setParentId(request.getParentId());

        Comment saved = commentRepository.save(comment);

        return toCommentResponse(saved, user, httpRequest);
    }

    /**
     * 扁平返回所有评论，前端自行根据parentId渲染树形结构
     */
    public Page<CommentResponse> getComments(Long postId, int page, int size, HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);

        return comments.map(comment -> {
            User user = userRepository.findById(comment.getUserId()).orElse(null);
            return toCommentResponse(comment, user, httpRequest);
        });
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此评论");
        }

        commentRepository.delete(comment);
    }

    // 管理员删除评论
    public void deleteByAdmin(Long commentId, Long adminId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        commentRepository.delete(comment);
    }

    private CommentResponse toCommentResponse(Comment comment, User user, HttpServletRequest request) {
        CommentResponse.UserInfo userInfo = null;
        if (user != null) {
            userInfo = new CommentResponse.UserInfo(user.getId(), user.getNickname(), getAvatarUrl(user, request));
        }
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                userInfo,
                comment.getParentId(),
                comment.getCreatedAt()
        );
    }

    private String getAvatarUrl(User user, HttpServletRequest request) {
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            return null;
        }
        try {
            Long fileId = Long.parseLong(user.getAvatar());
            return fileService.getFileUrl(fileId, request);
        } catch (NumberFormatException e) {
            return user.getAvatar();
        }
    }
}
