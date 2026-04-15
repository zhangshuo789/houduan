package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.ReadMessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.ConversationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UnreadCountResponse;
import com.volleyball.volleyballcommunitybackend.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/api/message/conversations")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getConversations(
            Authentication authentication,
            Pageable pageable,
            HttpServletRequest request) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<ConversationResponse> conversations = messageService.getConversations(currentUserId, pageable, request);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/api/message/private/{userId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getPrivateMessages(
            @PathVariable Long userId,
            Authentication authentication,
            Pageable pageable,
            HttpServletRequest request) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<MessageResponse> messages = messageService.getPrivateMessages(currentUserId, userId, pageable, request);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/api/message/private/{userId}")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long userId,
            @Valid @RequestBody MessageRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long currentUserId = (Long) authentication.getPrincipal();
        MessageResponse message = messageService.sendMessage(currentUserId, userId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("发送成功", message));
    }

    @PostMapping("/api/message/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestBody ReadMessageRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        messageService.markAsRead(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("标记已读", null));
    }

    @GetMapping("/api/message/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        UnreadCountResponse count = messageService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
