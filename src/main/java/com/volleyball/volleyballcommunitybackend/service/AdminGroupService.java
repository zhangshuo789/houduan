package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.response.GroupListResponse;
import com.volleyball.volleyballcommunitybackend.entity.ChatGroup;
import com.volleyball.volleyballcommunitybackend.entity.GroupMember;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.repository.ChatGroupRepository;
import com.volleyball.volleyballcommunitybackend.repository.GroupMemberRepository;
import com.volleyball.volleyballcommunitybackend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminGroupService {

    private final ChatGroupRepository chatGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageRepository messageRepository;

    public AdminGroupService(ChatGroupRepository chatGroupRepository,
                             GroupMemberRepository groupMemberRepository,
                             MessageRepository messageRepository) {
        this.chatGroupRepository = chatGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.messageRepository = messageRepository;
    }

    // 获取所有群列表
    public Page<GroupListResponse> getGroupList(Pageable pageable) {
        return chatGroupRepository.findAll(pageable).map(group -> {
            int memberCount = (int) groupMemberRepository.countByGroupId(group.getId());
            return new GroupListResponse(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getOwnerId(),
                    memberCount,
                    group.getCreatedAt()
            );
        });
    }

    // 更换群主
    @Transactional
    public void changeOwner(Long groupId, Long newOwnerId) {
        // Verify the group exists
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // Verify the new owner is a member of the group
        GroupMember newOwner = groupMemberRepository.findByGroupIdAndUserId(groupId, newOwnerId)
                .orElseThrow(() -> new RuntimeException("该用户不是群成员"));

        // Find and demote the current owner
        GroupMember currentOwner = groupMemberRepository.findByGroupIdAndUserId(groupId, group.getOwnerId())
                .orElse(null);
        if (currentOwner != null) {
            currentOwner.setRole("MEMBER");
            groupMemberRepository.save(currentOwner);
        }

        // Set new owner
        newOwner.setRole("OWNER");
        groupMemberRepository.save(newOwner);

        // Update group's ownerId
        group.setOwnerId(newOwnerId);
        chatGroupRepository.save(group);
    }

    // 解散群
    @Transactional
    public void dissolveGroup(Long groupId) {
        // Verify the group exists
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        // Delete all group members first
        groupMemberRepository.deleteByGroupId(groupId);

        // Delete all group messages
        List<Message> groupMessages = messageRepository.findByTypeAndTargetIdOrderByCreatedAtDesc(
                "group", groupId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        messageRepository.deleteAll(groupMessages);

        // Delete the group
        chatGroupRepository.delete(group);
    }
}
