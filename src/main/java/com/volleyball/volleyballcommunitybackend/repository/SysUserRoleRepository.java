package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    boolean existsByUserIdAndRoleName(Long userId, String roleName);
    void deleteByUserId(Long userId);
}
