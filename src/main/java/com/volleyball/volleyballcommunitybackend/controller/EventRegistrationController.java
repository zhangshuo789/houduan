package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRegistrationRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.service.EventRegistrationService;
import com.volleyball.volleyballcommunitybackend.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;
    private final EventService eventService;

    public EventRegistrationController(EventRegistrationService eventRegistrationService, EventService eventService) {
        this.eventRegistrationService = eventRegistrationService;
        this.eventService = eventService;
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse<EventRegistrationResponse>> registerForEvent(
            @PathVariable Long id,
            @RequestBody EventRegistrationRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        EventRegistrationResponse registration = eventRegistrationService.register(id, currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("报名成功", registration));
    }

    @GetMapping("/{id}/registration")
    public ResponseEntity<ApiResponse<Page<EventRegistrationResponse>>> getRegistrationList(
            @PathVariable Long id,
            Authentication authentication,
            Pageable pageable) {
        Long currentUserId = (Long) authentication.getPrincipal();
        if (!eventService.isEventOrganizer(id, currentUserId)) {
            throw new RuntimeException("无权限查看报名列表");
        }
        Page<EventRegistrationResponse> list = eventRegistrationService.getRegistrations(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{id}/registration/{regId}")
    public ResponseEntity<ApiResponse<EventRegistrationResponse>> approveRegistration(
            @PathVariable Long id,
            @PathVariable Long regId,
            @RequestParam boolean approved,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        if (!eventService.isEventOrganizer(id, currentUserId)) {
            throw new RuntimeException("无权限审核报名");
        }
        EventRegistrationResponse registration = eventRegistrationService.approveRegistration(regId, currentUserId, approved);
        return ResponseEntity.ok(ApiResponse.success(approved ? "审核通过" : "审核拒绝", registration));
    }
}
