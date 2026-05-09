package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.FeedbackReplyRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.FeedbackRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.FeedbackResponse;
import com.volleyball.volleyballcommunitybackend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * 提交反馈（用户）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> submit(
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        FeedbackResponse feedback = feedbackService.submit(request, userId);
        return ResponseEntity.ok(ApiResponse.success("反馈提交成功", feedback));
    }

    /**
     * 我的反馈列表（用户）
     */
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getMyFeedback(
            Pageable pageable,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Page<FeedbackResponse> list = feedbackService.getMyFeedback(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 所有反馈列表（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getAll(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<FeedbackResponse> list = feedbackService.getAll(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 回复反馈（管理员）
     */
    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> reply(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackReplyRequest request,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        FeedbackResponse feedback = feedbackService.reply(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success("回复成功", feedback));
    }

    /**
     * 关闭反馈（管理员）
     */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable Long id) {
        feedbackService.close(id);
        return ResponseEntity.ok(ApiResponse.success("反馈已关闭", null));
    }
}
