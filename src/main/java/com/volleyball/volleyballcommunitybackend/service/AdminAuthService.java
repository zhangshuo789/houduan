package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.SysRole;
import com.volleyball.volleyballcommunitybackend.entity.SysUserRole;
import com.volleyball.volleyballcommunitybackend.repository.SysRoleRepository;
import com.volleyball.volleyballcommunitybackend.repository.SysUserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminAuthService {

    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRoleRepository sysRoleRepository;

    public AdminAuthService(SysUserRoleRepository sysUserRoleRepository, SysRoleRepository sysRoleRepository) {
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysRoleRepository = sysRoleRepository;
    }

    // 判断用户是否为管理员
    public boolean isAdmin(Long userId) {
        return sysUserRoleRepository.existsByUserIdAndRoleName(userId, "ADMIN");
    }

    // 获取用户角色列表
    public List<String> getRoles(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(userId);
        List<String> roleNames = new ArrayList<>();
        for (SysUserRole ur : userRoles) {
            Optional<SysRole> role = sysRoleRepository.findById(ur.getRoleId());
            role.ifPresent(r -> roleNames.add(r.getName()));
        }
        return roleNames;
    }
}
