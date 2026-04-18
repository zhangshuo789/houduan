package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.UpdateUserRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.FeedResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserProfileStatsResponse;
import com.volleyball.volleyballcommunitybackend.entity.FileEntity;
import com.volleyball.volleyballcommunitybackend.entity.Follow;
import com.volleyball.volleyballcommunitybackend.entity.Post;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.PostRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FileService fileService;
    private final FollowService followService;
    private final PostRepository postRepository;

    public UserService(UserRepository userRepository, FileService fileService,
                       FollowService followService, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.followService = followService;
        this.postRepository = postRepository;
    }

    public UserResponse getUserById(Long id, HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return toUserResponse(user, request);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarFileId() != null) {
            user.setAvatar(request.getAvatarFileId().toString());
        }

        User saved = userRepository.save(user);
        return toUserResponse(saved, httpRequest);
    }

    public UserProfileStatsResponse getUserStats(Long userId) {
        long followCount = followService.getFollowCount(userId);
        long followerCount = followService.getFollowerCount(userId);
        long friendCount = followService.getFriendCount(userId);
        long postCount = postRepository.countByUserId(userId);

        return new UserProfileStatsResponse(followCount, followerCount, postCount, friendCount);
    }

    public Page<FeedResponse> getUserPosts(Long userId, Pageable pageable, HttpServletRequest request) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(post -> {
                    FeedResponse feed = new FeedResponse();
                    feed.setPostId(post.getId());
                    feed.setTitle(post.getTitle());
                    feed.setCreatedAt(post.getCreatedAt());
                    User user = userRepository.findById(post.getUserId())
                            .orElseThrow(() -> new RuntimeException("用户不存在"));
                    String avatarUrl = getAvatarUrl(user, request);
                    feed.setUser(new UserResponse(
                            user.getId(), user.getUsername(), user.getNickname(),
                            avatarUrl, user.getBio(), user.getCreatedAt(), null
                    ));
                    return feed;
                });
    }

    public Page<FeedResponse> getUserFeed(Long userId, Pageable pageable, HttpServletRequest request) {
        // 查询该用户关注的人的帖子
        Page<Follow> following = followService.getFollowingForFeed(userId, pageable);
        List<Long> followingIds = following.getContent().stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());

        if (followingIds.isEmpty()) {
            return Page.empty();
        }

        // 使用自定义查询获取关注的人的帖子
        return postRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable)
                .map(post -> {
                    FeedResponse feed = new FeedResponse();
                    feed.setPostId(post.getId());
                    feed.setTitle(post.getTitle());
                    feed.setCreatedAt(post.getCreatedAt());
                    User user = userRepository.findById(post.getUserId())
                            .orElseThrow(() -> new RuntimeException("用户不存在"));
                    String avatarUrl = getAvatarUrl(user, request);
                    feed.setUser(new UserResponse(
                            user.getId(), user.getUsername(), user.getNickname(),
                            avatarUrl, user.getBio(), user.getCreatedAt(), null
                    ));
                    return feed;
                });
    }

    private UserResponse toUserResponse(User user, HttpServletRequest request) {
        String avatarUrl = getAvatarUrl(user, request);
        UserProfileStatsResponse stats = getUserStats(user.getId());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                avatarUrl,
                user.getBio(),
                user.getCreatedAt(),
                stats
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