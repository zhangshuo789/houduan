package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.Like;
import com.volleyball.volleyballcommunitybackend.entity.Post;
import com.volleyball.volleyballcommunitybackend.repository.LikeRepository;
import com.volleyball.volleyballcommunitybackend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    public void like(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("已点赞");
        }

        Like like = new Like();
        like.setUserId(userId);
        like.setPostId(postId);
        likeRepository.save(like);
    }

    @Transactional
    public void unlike(Long postId, Long userId) {
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("未点赞");
        }
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public Boolean getLikeStatus(Long postId, Long userId) {
        if (userId == null) {
            return null;
        }
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    public Long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
