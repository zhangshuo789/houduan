package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRegistrationRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.EventRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventResponse;
import com.volleyball.volleyballcommunitybackend.service.EventRegistrationService;
import com.volleyball.volleyballcommunitybackend.service.EventService;
import com.volleyball.volleyballcommunitybackend.service.EventSubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;
    private final EventRegistrationService registrationService;
    private final EventSubscriptionService subscriptionService;

    public EventController(EventService eventService, EventRegistrationService registrationService,
                           EventSubscriptionService subscriptionService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * 赛事列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEventList(
            Pageable pageable,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<EventResponse> list = eventService.getEventList(pageable, userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 赛事详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable Long id,
            HttpServletRequest request,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        EventResponse event = eventService.getEventById(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    /**
     * 创建赛事
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        EventResponse event = eventService.createEvent(request, userId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("赛事创建成功", event));
    }

    /**
     * 更新赛事
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        EventResponse event = eventService.updateEvent(id, request, userId, false);
        return ResponseEntity.ok(ApiResponse.success("赛事更新成功", event));
    }

    /**
     * 删除赛事
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        eventService.deleteEvent(id, userId, false);
        return ResponseEntity.ok(ApiResponse.success("赛事删除成功", null));
    }

    /**
     * 报名（自动分配 bracket 位置）
     */
    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse<EventRegistrationResponse>> register(
            @PathVariable Long id,
            @Valid @RequestBody EventRegistrationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        EventRegistrationResponse registration = registrationService.register(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("报名成功", registration));
    }

    /**
     * 查看报名列表（组织者）
     */
    @GetMapping("/{id}/registration")
    public ResponseEntity<ApiResponse<Page<EventRegistrationResponse>>> getRegistrations(
            @PathVariable Long id,
            Pageable pageable,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (!eventService.isEventOrganizer(id, userId)) {
            throw new RuntimeException("无权限查看报名列表");
        }
        Page<EventRegistrationResponse> list = registrationService.getRegistrations(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 订阅赛事（接收通知）
     */
    @PostMapping("/{id}/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        subscriptionService.subscribe(id, userId);
        return ResponseEntity.ok(ApiResponse.success("订阅成功", null));
    }

    /**
     * 取消订阅
     */
    @DeleteMapping("/{id}/subscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        subscriptionService.unsubscribe(id, userId);
        return ResponseEntity.ok(ApiResponse.success("取消订阅成功", null));
    }
}
