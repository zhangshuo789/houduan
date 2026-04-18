package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.GroupMember;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.repository.GroupMemberRepository;
import com.volleyball.volleyballcommunitybackend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminGroupService {

    private final GroupMemberRepository groupMemberRepository;
    private final MessageRepository messageRepository;

    public AdminGroupService(GroupMemberRepository groupMemberRepository,
                             MessageRepository messageRepository) {
        this.groupMemberRepository = groupMemberRepository;
        this.messageRepository = messageRepository;
    }

    // 获取所有群列表
    public Page<Message> getGroupList(Pageable pageable) {
        // Groups are stored as Message with type='group'
        return messageRepository.findByTypeOrderByCreatedAtDesc("group", pageable);
    }

    // 更换群主
    @Transactional
    public void changeOwner(Long groupId, Long newOwnerId) {
        // Verify the group exists
        Message groupMessage = messageRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        if (!"group".equals(groupMessage.getType())) {
            throw new RuntimeException("该消息不是群聊");
        }

        // Verify the new owner is a member of the group
        GroupMember newOwner = groupMemberRepository.findByGroupIdAndUserId(groupId, newOwnerId)
                .orElseThrow(() -> new RuntimeException("该用户不是群成员"));

        // Update the owner role
        newOwner.setRole("OWNER");
        groupMemberRepository.save(newOwner);

        // Find and demote the current owner (if there is one)
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        for (GroupMember member : members) {
            if (!member.getUserId().equals(newOwnerId) && "OWNER".equals(member.getRole())) {
                member.setRole("MEMBER");
                groupMemberRepository.save(member);
            }
        }
    }

    // 解散群
    @Transactional
    public void dissolveGroup(Long groupId) {
        // Verify the group exists
        Message groupMessage = messageRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        if (!"group".equals(groupMessage.getType())) {
            throw new RuntimeException("该消息不是群聊");
        }

        // Delete all group members first
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        groupMemberRepository.deleteAll(members);

        // Delete all group messages
        List<Message> groupMessages = messageRepository.findByTypeAndTargetIdOrderByCreatedAtDesc(
                "group", groupId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        messageRepository.deleteAll(groupMessages);

        // Delete the group message itself
        messageRepository.delete(groupMessage);
    }
}
