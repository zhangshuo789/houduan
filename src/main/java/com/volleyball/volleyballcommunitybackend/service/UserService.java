package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.UpdateUserRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.UserResponse;
import com.volleyball.volleyballcommunitybackend.entity.FileEntity;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FileService fileService;

    public UserService(UserRepository userRepository, FileService fileService) {
        this.userRepository = userRepository;
        this.fileService = fileService;
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

    private UserResponse toUserResponse(User user, HttpServletRequest request) {
        String avatarUrl = null;
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                Long fileId = Long.parseLong(user.getAvatar());
                avatarUrl = fileService.getFileUrl(fileId, request);
            } catch (NumberFormatException e) {
                // 如果不是数字，说明是旧的URL格式，直接使用
                avatarUrl = user.getAvatar();
            }
        }

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                avatarUrl,
                user.getBio(),
                user.getCreatedAt()
        );
    }
}
