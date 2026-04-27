package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.AiMessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.AiConversationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.AiMessageResponse;
import com.volleyball.volleyballcommunitybackend.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/ai/conversations")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiConversationResponse>> createConversation(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AiConversationResponse conversation = aiService.createConversation(userId);
        return ResponseEntity.ok(ApiResponse.success("会话创建成功", conversation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiConversationResponse>>> getConversations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<AiConversationResponse> conversations = aiService.getConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        aiService.deleteConversation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("会话已删除", null));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<AiMessageResponse>>> getMessages(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<AiMessageResponse> messages = aiService.getMessages(id, userId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping(value = "/{id}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AiMessageResponse>> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody AiMessageRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AiMessageResponse message = aiService.sendMessage(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping(value = "/{id}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @PathVariable Long id,
            @Valid @RequestBody AiMessageRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return aiService.sendMessageStream(id, userId, request);
    }
}