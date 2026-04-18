package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    void deleteByUserId(Long userId);
    List<SysUserRole> findByUserId(Long userId);

    @Query("SELECT COUNT(ur) > 0 FROM SysUserRole ur JOIN SysRole r ON ur.roleId = r.id WHERE ur.userId = :userId AND r.name = :roleName")
    boolean existsByUserIdAndRoleName(@Param("userId") Long userId, @Param("roleName") String roleName);
}
