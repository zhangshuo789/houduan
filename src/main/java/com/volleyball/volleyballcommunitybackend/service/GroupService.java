package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.GroupRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupMemberResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.entity.GroupMember;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.entity.MessageRead;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import org.springframework.data.domain.Page;
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
    private final UserRepository userRepository;
    private final SseService sseService;

    public GroupService(MessageRepository messageRepository, MessageReadRepository messageReadRepository,
                        GroupMemberRepository groupMemberRepository, UserRepository userRepository,
                        SseService sseService) {
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
    }

    @Transactional
    public GroupResponse createGroup(Long creatorId, GroupRequest request) {
        // 创建群聊会话
        Message groupMessage = new Message();
        groupMessage.setSenderId(creatorId);
        groupMessage.setType("group");
        groupMessage.setContent("群聊创建");
        groupMessage.setTargetId(0L); // 临时占位
        Message saved = messageRepository.save(groupMessage);

        Long groupId = saved.getId();

        // 更新群ID（因为我们用message id作为group id）
        groupMessage.setTargetId(groupId);
        messageRepository.save(groupMessage);

        // 创建者加群
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(groupId);
        ownerMember.setUserId(creatorId);
        ownerMember.setRole("OWNER");
        groupMemberRepository.save(ownerMember);

        // 添加其他成员
        for (Long memberId : request.getMemberIds()) {
            if (!memberId.equals(creatorId)) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setUserId(memberId);
                member.setRole("MEMBER");
                groupMemberRepository.save(member);
            }
        }

        GroupResponse response = new GroupResponse();
        response.setId(groupId);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setType("group");
        response.setMemberCount(request.getMemberIds().size() + 1);
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }

    public GroupResponse getGroupInfo(Long groupId) {
        Message groupMessage = messageRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        GroupResponse response = new GroupResponse();
        response.setId(groupId);
        response.setName(groupMessage.getContent()); // 复用content存群名
        response.setDescription("");
        response.setType("group");
        response.setMemberCount((int) memberCount);
        response.setCreatedAt(groupMessage.getCreatedAt());
        return response;
    }

    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(this::toGroupMemberResponse)
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
    public MessageResponse sendGroupMessage(Long senderId, Long groupId, MessageRequest request) {
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

        MessageResponse response = toMessageResponse(saved);

        // 推送给所有在线群成员
        for (GroupMember m : members) {
            if (!m.getUserId().equals(senderId)) {
                sseService.sendMessageToUser(m.getUserId(), "newGroupMessage", response);
            }
        }

        return response;
    }

    public Page<MessageResponse> getGroupMessages(Long groupId, Pageable pageable) {
        return messageRepository.findByTypeAndTargetIdOrderByCreatedAtDesc("group", groupId, pageable)
                .map(this::toMessageResponse);
    }

    private GroupMemberResponse toGroupMemberResponse(GroupMember member) {
        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        GroupMemberResponse response = new GroupMemberResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRole(member.getRole());
        response.setBanned(member.getBanned());
        response.setJoinedAt(member.getJoinedAt());
        return response;
    }

    private MessageResponse toMessageResponse(Message message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return new MessageResponse(
                message.getId(),
                sender.getId(),
                sender.getNickname(),
                sender.getAvatar(),
                message.getType(),
                message.getTargetId(),
                message.getContent(),
                message.getCreatedAt(),
                true
        );
    }
}
