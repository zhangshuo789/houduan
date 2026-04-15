package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.response.FollowStatusResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserResponse;
import com.volleyball.volleyballcommunitybackend.entity.Follow;
import com.volleyball.volleyballcommunitybackend.entity.Friendship;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PrivacyService privacyService;
    private final FileService fileService;

    public FollowService(FollowRepository followRepository, FriendshipRepository friendshipRepository,
                         UserRepository userRepository, PostRepository postRepository,
                         PrivacyService privacyService, FileService fileService) {
        this.followRepository = followRepository;
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.privacyService = privacyService;
        this.fileService = fileService;
    }

    @Transactional
    public void followUser(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new RuntimeException("已经关注了该用户");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followRepository.save(follow);

        // 检查是否互关
        if (followRepository.existsByFollowerIdAndFolloweeId(followeeId, followerId)) {
            // 双向都创建friendship记录
            if (!friendshipRepository.existsByUserIdAndFriendId(followerId, followeeId)) {
                Friendship f1 = new Friendship();
                f1.setUserId(followerId);
                f1.setFriendId(followeeId);
                friendshipRepository.save(f1);
            }
            if (!friendshipRepository.existsByUserIdAndFriendId(followeeId, followerId)) {
                Friendship f2 = new Friendship();
                f2.setUserId(followeeId);
                f2.setFriendId(followerId);
                friendshipRepository.save(f2);
            }
        }
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followeeId) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
        friendshipRepository.deleteByUserIdAndFriendId(followerId, followeeId);
        friendshipRepository.deleteByUserIdAndFriendId(followeeId, followerId);
    }

    public FollowStatusResponse getFollowStatus(Long userId, Long targetUserId) {
        boolean following = followRepository.existsByFollowerIdAndFolloweeId(userId, targetUserId);
        boolean followedBy = followRepository.existsByFollowerIdAndFolloweeId(targetUserId, userId);
        boolean mutualFollow = following && followedBy;
        return new FollowStatusResponse(following, followedBy, mutualFollow);
    }

    public Page<UserResponse> getFollowingList(Long userId, Long viewerId, Pageable pageable, HttpServletRequest request) {
        if (!privacyService.isFollowListVisible(userId, viewerId)) {
            return Page.empty();
        }
        return followRepository.findByFollowerId(userId, pageable)
                .map(f -> toUserResponse(f.getFolloweeId(), request));
    }

    public Page<UserResponse> getFollowerList(Long userId, Long viewerId, Pageable pageable, HttpServletRequest request) {
        if (!privacyService.isFollowerListVisible(userId, viewerId)) {
            return Page.empty();
        }
        return followRepository.findByFolloweeId(userId, pageable)
                .map(f -> toUserResponse(f.getFollowerId(), request));
    }

    public Page<UserResponse> getFriendsList(Long userId, Pageable pageable, HttpServletRequest request) {
        return friendshipRepository.findByUserId(userId, pageable)
                .map(f -> toUserResponse(f.getFriendId(), request));
    }

    public long getFollowCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    public long getFollowerCount(Long userId) {
        return followRepository.countByFolloweeId(userId);
    }

    public long getFriendCount(Long userId) {
        return friendshipRepository.countByUserId(userId);
    }

    public Page<Follow> getFollowingForFeed(Long userId, Pageable pageable) {
        return followRepository.findByFollowerId(userId, pageable);
    }

    private UserResponse toUserResponse(Long targetUserId, HttpServletRequest request) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                getAvatarUrl(user, request),
                user.getBio(),
                user.getCreatedAt(),
                null
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
