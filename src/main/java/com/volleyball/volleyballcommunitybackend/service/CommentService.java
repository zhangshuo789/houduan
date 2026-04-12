package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.CommentRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.CommentResponse;
import com.volleyball.volleyballcommunitybackend.entity.Comment;
import com.volleyball.volleyballcommunitybackend.entity.Post;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.CommentRepository;
import com.volleyball.volleyballcommunitybackend.repository.PostRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public CommentResponse addComment(Long postId, CommentRequest request, Long userId) {
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
        comment.setContent(request.getContent());
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setParentId(request.getParentId());

        Comment saved = commentRepository.save(comment);

        return toCommentResponse(saved, user);
    }

    public Page<CommentResponse> getComments(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> topLevelComments = commentRepository.findByPostIdAndParentIdIsNull(postId, pageable);

        return topLevelComments.map(comment -> {
            User user = userRepository.findById(comment.getUserId()).orElse(null);
            List<Comment> replies = commentRepository.findByParentId(comment.getId());
            List<CommentResponse> replyResponses = replies.stream()
                    .map(reply -> {
                        User replyUser = userRepository.findById(reply.getUserId()).orElse(null);
                        return toCommentResponse(reply, replyUser);
                    })
                    .collect(Collectors.toList());

            CommentResponse response = toCommentResponse(comment, user);
            response.setReplies(replyResponses);
            return response;
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

    private CommentResponse toCommentResponse(Comment comment, User user) {
        CommentResponse.UserInfo userInfo = null;
        if (user != null) {
            userInfo = new CommentResponse.UserInfo(user.getId(), user.getNickname(), user.getAvatar());
        }
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                userInfo,
                comment.getParentId(),
                comment.getCreatedAt(),
                null
        );
    }
}
