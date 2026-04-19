package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.GroupRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.UpdateGroupRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupMemberResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @Valid @RequestBody GroupRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        GroupResponse group = groupService.createGroup(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", group));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<GroupResponse>>> getMyGroups(
            Pageable pageable,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<GroupResponse> groups = groupService.getUserGroups(currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupInfo(@PathVariable Long id) {
        GroupResponse group = groupService.getGroupInfo(id);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        GroupResponse group = groupService.updateGroup(currentUserId, id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", group));
    }

    @PutMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<Void>> updateGroupAvatar(
            @PathVariable Long id,
            @RequestParam String avatarFileId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.updateGroupAvatar(currentUserId, id, avatarFileId);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.deleteGroup(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.success("解散成功", null));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getGroupMembers(
            @PathVariable Long id,
            HttpServletRequest request) {
        List<GroupMemberResponse> members = groupService.getGroupMembers(id, request);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.addMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("添加成功", null));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.removeMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("移除成功", null));
    }

    @PostMapping("/{id}/members/{userId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.leaveGroup(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.success("退群成功", null));
    }

    @PostMapping("/{id}/members/{userId}/ban")
    public ResponseEntity<ApiResponse<Void>> banMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.banMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("禁言成功", null));
    }

    @DeleteMapping("/{id}/members/{userId}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.unbanMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("解除禁言成功", null));
    }

    @PostMapping("/{id}/members/{userId}/admin")
    public ResponseEntity<ApiResponse<Void>> setAdmin(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam boolean setAdmin,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.setAdmin(currentUserId, id, userId, setAdmin);
        return ResponseEntity.ok(ApiResponse.success(setAdmin ? "设置管理员成功" : "取消管理员成功", null));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<ApiResponse<Void>> transferOwner(
            @PathVariable Long id,
            @RequestParam Long newOwnerId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.transferOwner(currentUserId, id, newOwnerId);
        return ResponseEntity.ok(ApiResponse.success("转让成功", null));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getGroupMessages(
            @PathVariable Long id,
            Pageable pageable,
            HttpServletRequest request) {
        Page<MessageResponse> messages = groupService.getGroupMessages(id, pageable, request);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendGroupMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long currentUserId = (Long) authentication.getPrincipal();
        MessageResponse message = groupService.sendGroupMessage(currentUserId, id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("发送成功", message));
    }
}
