package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.GroupRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.UpdateGroupRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupMemberResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.entity.ChatGroup;
import com.volleyball.volleyballcommunitybackend.entity.GroupMember;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.entity.MessageRead;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.ChatGroupRepository;
import com.volleyball.volleyballcommunitybackend.repository.GroupMemberRepository;
import com.volleyball.volleyballcommunitybackend.repository.MessageReadRepository;
import com.volleyball.volleyballcommunitybackend.repository.MessageRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final UserRepository userRepository;
    private final SseService sseService;
    private final FileService fileService;

    public GroupService(MessageRepository messageRepository, MessageReadRepository messageReadRepository,
                        GroupMemberRepository groupMemberRepository, ChatGroupRepository chatGroupRepository,
                        UserRepository userRepository, SseService sseService, FileService fileService) {
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
        this.fileService = fileService;
    }

    @Transactional
    public GroupResponse createGroup(Long creatorId, GroupRequest request) {
        // 创建群组
        ChatGroup group = new ChatGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setOwnerId(creatorId);
        ChatGroup saved = chatGroupRepository.save(group);

        Long groupId = saved.getId();

        // 创建者加群
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(groupId);
        ownerMember.setUserId(creatorId);
        ownerMember.setRole("OWNER");
        groupMemberRepository.save(ownerMember);

        // 添加其他成员（可选）
        int memberCount = 1; // 群主自己
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(creatorId)) {
                    GroupMember member = new GroupMember();
                    member.setGroupId(groupId);
                    member.setUserId(memberId);
                    member.setRole("MEMBER");
                    groupMemberRepository.save(member);
                    memberCount++;
                }
            }
        }

        GroupResponse response = new GroupResponse();
        response.setId(groupId);
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());
        response.setType("group");
        response.setMemberCount(memberCount);
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }

    public GroupResponse getGroupInfo(Long groupId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        GroupResponse response = new GroupResponse();
        response.setId(groupId);
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setAvatar(getAvatarUrl(group.getAvatar()));
        response.setType("group");
        response.setMemberCount((int) memberCount);
        response.setCreatedAt(group.getCreatedAt());
        return response;
    }

    @Transactional
    public GroupResponse updateGroup(Long operatorId, Long groupId, UpdateGroupRequest request) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // 检查权限：群主或管理员可修改
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以修改群信息");
        }

        // 群主可修改所有信息，管理员只能修改部分
        if (request.getName() != null && !"OWNER".equals(operator.getRole())) {
            throw new RuntimeException("只有群主可以修改群名称");
        }

        if (request.getName() != null) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        chatGroupRepository.save(group);

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        GroupResponse response = new GroupResponse();
        response.setId(groupId);
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setAvatar(getAvatarUrl(group.getAvatar()));
        response.setType("group");
        response.setMemberCount((int) memberCount);
        response.setCreatedAt(group.getCreatedAt());
        return response;
    }

    @Transactional
    public void updateGroupAvatar(Long operatorId, Long groupId, String avatarFileId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // 检查权限：群主或管理员
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以修改群头像");
        }

        group.setAvatar(avatarFileId);
        chatGroupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long operatorId, Long groupId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // 只有群主可以解散群
        if (!group.getOwnerId().equals(operatorId)) {
            throw new RuntimeException("只有群主可以解散群聊");
        }

        // 删除所有群成员
        groupMemberRepository.deleteByGroupId(groupId);
        // 删除群组
        chatGroupRepository.delete(group);
    }

    public List<GroupMemberResponse> getGroupMembers(Long groupId, HttpServletRequest request) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(member -> toGroupMemberResponse(member, request))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以邀请成员");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)) {
            throw new RuntimeException("该用户已在群中");
        }

        GroupMember newMember = new GroupMember();
        newMember.setGroupId(groupId);
        newMember.setUserId(targetUserId);
        newMember.setRole("MEMBER");
        groupMemberRepository.save(newMember);
    }

    @Transactional
    public void removeMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole())) {
            throw new RuntimeException("只有群主可以移除成员");
        }

        if (operatorId.equals(targetUserId)) {
            throw new RuntimeException("群主不能被移除");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if ("OWNER".equals(member.getRole())) {
            throw new RuntimeException("群主不能退群，请先转让群或解散群");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Transactional
    public void banMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以禁言");
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("该用户不在群中"));

        target.setBanned(true);
        groupMemberRepository.save(target);
    }

    @Transactional
    public void unbanMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以解除禁言");
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("该用户不在群中"));

        target.setBanned(false);
        groupMemberRepository.save(target);
    }

    @Transactional
    public void setAdmin(Long operatorId, Long groupId, Long targetUserId, boolean setAdmin) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        // 只有群主可以设置管理员
        if (!"OWNER".equals(operator.getRole())) {
            throw new RuntimeException("只有群主可以设置管理员");
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("该用户不在群中"));

        target.setRole(setAdmin ? "ADMIN" : "MEMBER");
        groupMemberRepository.save(target);
    }

    @Transactional
    public void transferOwner(Long operatorId, Long groupId, Long newOwnerId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // 只有群主可以转让
        if (!group.getOwnerId().equals(operatorId)) {
            throw new RuntimeException("只有群主可以转让群");
        }

        GroupMember newOwnerMember = groupMemberRepository.findByGroupIdAndUserId(groupId, newOwnerId)
                .orElseThrow(() -> new RuntimeException("该用户不在群中"));

        // 将新群主设为ADMIN
        newOwnerMember.setRole("ADMIN");
        groupMemberRepository.save(newOwnerMember);

        // 将原群主改为MEMBER
        GroupMember ownerMember = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你是群主但不在群中"));
        ownerMember.setRole("MEMBER");
        groupMemberRepository.save(ownerMember);

        // 更新群的ownerId
        group.setOwnerId(newOwnerId);
        chatGroupRepository.save(group);
    }

    @Transactional
    public MessageResponse sendGroupMessage(Long senderId, Long groupId, MessageRequest request, HttpServletRequest httpRequest) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, senderId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (member.getBanned()) {
            throw new RuntimeException("你已被禁言");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setType("group");
        message.setTargetId(groupId);
        message.setContent(request.getContent());
        Message saved = messageRepository.save(message);

        // 所有成员（包括发送者）标记为已读
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        for (GroupMember m : members) {
            MessageRead read = new MessageRead();
            read.setMessageId(saved.getId());
            read.setUserId(m.getUserId());
            read.setReadAt(LocalDateTime.now()); // 发送者即已读
            messageReadRepository.save(read);
        }

        MessageResponse response = toMessageResponse(saved, httpRequest);

        // 推送给所有在线群成员
        for (GroupMember m : members) {
            if (!m.getUserId().equals(senderId)) {
                sseService.sendMessageToUser(m.getUserId(), "newGroupMessage", response);
            }
        }

        return response;
    }

    public Page<MessageResponse> getGroupMessages(Long groupId, Pageable pageable, HttpServletRequest request) {
        return messageRepository.findByTypeAndTargetIdOrderByCreatedAtDesc("group", groupId, pageable)
                .map(message -> toMessageResponse(message, request));
    }

    /**
     * 获取用户已加入的群聊列表
     */
    public Page<GroupResponse> getUserGroups(Long userId, Pageable pageable) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        List<Long> groupIds = memberships.stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toList());

        if (groupIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<GroupResponse> groups = groupIds.stream()
                .map(groupId -> {
                    ChatGroup group = chatGroupRepository.findById(groupId)
                            .orElseThrow(() -> new RuntimeException("群聊不存在"));
                    long memberCount = groupMemberRepository.countByGroupId(groupId);
                    GroupResponse response = new GroupResponse();
                    response.setId(groupId);
                    response.setName(group.getName());
                    response.setDescription(group.getDescription());
                    response.setAvatar(getAvatarUrl(group.getAvatar()));
                    response.setType("group");
                    response.setMemberCount((int) memberCount);
                    response.setCreatedAt(group.getCreatedAt());
                    return response;
                })
                .collect(Collectors.toList());

        // 分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), groups.size());
        List<GroupResponse> pageContent = start < groups.size()
            ? groups.subList(start, end)
            : java.util.Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, groups.size());
    }

    private GroupMemberResponse toGroupMemberResponse(GroupMember member, HttpServletRequest request) {
        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        GroupMemberResponse response = new GroupMemberResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setAvatar(getAvatarUrl(user.getAvatar()));
        response.setRole(member.getRole());
        response.setBanned(member.getBanned());
        response.setJoinedAt(member.getJoinedAt());
        return response;
    }

    private MessageResponse toMessageResponse(Message message, HttpServletRequest request) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return new MessageResponse(
                message.getId(),
                sender.getId(),
                sender.getNickname(),
                getAvatarUrl(sender.getAvatar()),
                message.getType(),
                message.getTargetId(),
                message.getContent(),
                message.getCreatedAt(),
                true
        );
    }

    private String getAvatarUrl(String avatar) {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        try {
            Long fileId = Long.parseLong(avatar);
            return "/api/file/" + fileId;
        } catch (NumberFormatException e) {
            return avatar;
        }
    }
}
