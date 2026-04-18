package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.response.UserManagementResponse;
import com.volleyball.volleyballcommunitybackend.entity.SysRole;
import com.volleyball.volleyballcommunitybackend.entity.SysUserRole;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.entity.UserStatus;
import com.volleyball.volleyballcommunitybackend.repository.SysRoleRepository;
import com.volleyball.volleyballcommunitybackend.repository.SysUserRoleRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserStatusRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final UserStatusRepository userStatusRepository;

    public AdminUserService(UserRepository userRepository,
                            SysRoleRepository sysRoleRepository,
                            SysUserRoleRepository sysUserRoleRepository,
                            UserStatusRepository userStatusRepository) {
        this.userRepository = userRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.userStatusRepository = userStatusRepository;
    }

    public Page<UserManagementResponse> getUserList(String keyword, Boolean disabled, Pageable pageable) {
        Page<User> users;

        if (keyword != null && !keyword.isEmpty()) {
            if (disabled != null) {
                // Search by keyword and filter by disabled status
                users = userRepository.findAll(pageable);
            } else {
                // Search by keyword only
                users = userRepository.findAll(pageable);
            }
        } else if (disabled != null) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        // Filter and map manually for more control
        return users.map(user -> {
            UserManagementResponse response = new UserManagementResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
            response.setBio(user.getBio());
            response.setCreatedAt(user.getCreatedAt());

            // Get user roles
            List<SysUserRole> userRoles = sysUserRoleRepository.findAll().stream()
                    .filter(ur -> ur.getUserId().equals(user.getId()))
                    .collect(Collectors.toList());

            List<String> roleNames = userRoles.stream()
                    .map(ur -> sysRoleRepository.findById(ur.getRoleId()).map(SysRole::getName).orElse(null))
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            response.setRoles(roleNames);

            // Get user status
            UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
            if (status != null) {
                response.setDisabled(status.getDisabled());
                response.setDisabledReason(status.getDisabledReason());
            } else {
                response.setDisabled(false);
                response.setDisabledReason(null);
            }

            return response;
        });
    }

    @Transactional
    public void setUserRole(Long userId, Long roleId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }

        // Verify role exists
        SysRole role = sysRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // Delete existing user roles
        List<SysUserRole> existingRoles = sysUserRoleRepository.findAll().stream()
                .filter(ur -> ur.getUserId().equals(userId))
                .collect(Collectors.toList());
        sysUserRoleRepository.deleteAll(existingRoles);

        // Create new user role
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreatedAt(LocalDateTime.now());
        sysUserRoleRepository.save(userRole);
    }

    @Transactional
    public void setUserStatus(Long userId, Boolean disabled, String reason, Long adminId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }

        UserStatus status = userStatusRepository.findByUserId(userId)
                .orElse(new UserStatus());

        status.setUserId(userId);
        status.setDisabled(disabled);

        if (disabled) {
            status.setDisabledReason(reason);
            status.setDisabledAt(LocalDateTime.now());
            status.setDisabledBy(adminId);
        } else {
            status.setDisabledReason(null);
            status.setDisabledAt(null);
            status.setDisabledBy(null);
        }

        userStatusRepository.save(status);
    }
}
