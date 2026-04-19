package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.LoginRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.RegisterRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.LoginResponse;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import com.volleyball.volleyballcommunitybackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FileService fileService;
    private final PrivacyService privacyService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       FileService fileService, PrivacyService privacyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.fileService = fileService;
        this.privacyService = privacyService;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());

        User saved = userRepository.save(user);

        // 创建默认隐私设置
        privacyService.getOrCreatePrivacySettings(saved.getId());

        return saved;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        String avatarUrl = null;
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                Long fileId = Long.parseLong(user.getAvatar());
                avatarUrl = fileService.getFileUrl(fileId, httpRequest);
            } catch (NumberFormatException e) {
                avatarUrl = user.getAvatar();
            }
        }

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                avatarUrl,
                "admin".equals(user.getUsername())
        );

        return new LoginResponse(token, userInfo);
    }
}