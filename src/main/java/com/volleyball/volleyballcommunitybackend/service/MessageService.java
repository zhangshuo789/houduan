package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.ReadMessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ConversationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UnreadCountResponse;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.entity.MessageRead;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import com.volleyball.volleyballcommunitybackend.util.SensitiveWordFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final SseService sseService;
    private final PrivacyService privacyService;
    private final FileService fileService;
    private final SensitiveWordFilter sensitiveWordFilter;

    public MessageService(MessageRepository messageRepository, MessageReadRepository messageReadRepository,
                          UserRepository userRepository, FollowRepository followRepository,
                          SseService sseService, PrivacyService privacyService, FileService fileService,
                          SensitiveWordFilter sensitiveWordFilter) {
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.sseService = sseService;
        this.privacyService = privacyService;
        this.fileService = fileService;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    @Transactional
    public MessageResponse sendMessage(Long senderId, Long receiverId, MessageRequest request, HttpServletRequest httpRequest) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发消息");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查接收者是否设置"仅好友接收"
        if (privacyService.isFriendsOnlyReceive(receiverId)) {
            boolean isFriend = followRepository.existsByFollowerIdAndFolloweeId(senderId, receiverId)
                    && followRepository.existsByFollowerIdAndFolloweeId(receiverId, senderId);
            if (!isFriend) {
                throw new RuntimeException("对方只接收好友消息");
            }
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setType("private");
        message.setTargetId(receiverId);
        message.setContent(sensitiveWordFilter.filter(request.getContent()));
        Message saved = messageRepository.save(message);

        // 发送者已读
        MessageRead senderRead = new MessageRead();
        senderRead.setMessageId(saved.getId());
        senderRead.setUserId(senderId);
        senderRead.setReadAt(LocalDateTime.now());
        messageReadRepository.save(senderRead);

        // 接收者未读
        MessageRead receiverRead = new MessageRead();
        receiverRead.setMessageId(saved.getId());
        receiverRead.setUserId(receiverId);
        messageReadRepository.save(receiverRead);

        MessageResponse response = toMessageResponse(saved, httpRequest);

        // SSE推送
        sseService.sendMessageToUser(receiverId, "newMessage", response);

        return response;
    }

    public Page<ConversationResponse> getConversations(Long userId, Pageable pageable, HttpServletRequest request) {
        Page<Long> conversationUserIds = messageRepository.findPrivateConversationUserIds(userId, pageable);

        return conversationUserIds.map(otherUserId -> {
            User otherUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Page<Message> lastMessages = messageRepository.findPrivateMessages(userId, otherUserId, Pageable.unpaged());
            Message lastMessage = lastMessages.hasContent() ? lastMessages.getContent().get(0) : null;

            long unreadCount = messageReadRepository.countByUserIdAndReadAtIsNull(userId);

            ConversationResponse conversation = new ConversationResponse();
            conversation.setOderId(otherUser.getId());
            conversation.setOderNickname(otherUser.getNickname());
            conversation.setOderAvatar(getAvatarUrl(otherUser, request));
            conversation.setLastMessage(lastMessage != null ? lastMessage.getContent() : "");
            conversation.setLastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null);
            conversation.setUnreadCount((int) unreadCount);
            return conversation;
        });
    }

    public Page<MessageResponse> getPrivateMessages(Long userId, Long otherUserId, Pageable pageable, HttpServletRequest request) {
        return messageRepository.findPrivateMessages(userId, otherUserId, pageable)
                .map(message -> toMessageResponse(message, request));
    }

    @Transactional
    public void markAsRead(Long userId, ReadMessageRequest request) {
        if (request.getConversationWithUserId() != null) {
            Long otherUserId = request.getConversationWithUserId();
            Page<Message> messages = messageRepository.findPrivateMessages(userId, otherUserId, Pageable.unpaged());
            List<Long> unreadMessageIds = messages.getContent().stream()
                    .filter(m -> messageReadRepository.findByMessageIdAndUserId(m.getId(), userId)
                            .map(r -> r.getReadAt() == null)
                            .orElse(false))
                    .map(Message::getId)
                    .collect(Collectors.toList());

            if (!unreadMessageIds.isEmpty()) {
                messageReadRepository.batchMarkAsRead(userId, unreadMessageIds, LocalDateTime.now());
            }
        } else if (request.getGroupId() != null) {
            // 群聊已读逻辑类似
        }
    }

    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = messageReadRepository.countByUserIdAndReadAtIsNull(userId);
        return new UnreadCountResponse(count);
    }

    private MessageResponse toMessageResponse(Message message, HttpServletRequest request) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Boolean isRead = messageReadRepository.findByMessageIdAndUserId(message.getId(), message.getTargetId())
                .map(r -> r.getReadAt() != null)
                .orElse(true);

        return new MessageResponse(
                message.getId(),
                sender.getId(),
                sender.getNickname(),
                getAvatarUrl(sender, request),
                message.getType(),
                message.getTargetId(),
                message.getContent(),
                message.getCreatedAt(),
                isRead
        );
    }

    private String getAvatarUrl(User user, HttpServletRequest request) {
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            return null;
        }
        try {
            Long fileId = Long.parseLong(user.getAvatar());
            return fileService.getFileUrl(fileId, request);
        } catch (NumberFormatException e) {
            return user.getAvatar();
        }
    }
}
