package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventResponse;
import com.volleyball.volleyballcommunitybackend.service.EventService;
import com.volleyball.volleyballcommunitybackend.service.EventSubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;
    private final EventSubscriptionService eventSubscriptionService;

    public EventController(EventService eventService, EventSubscriptionService eventSubscriptionService) {
        this.eventService = eventService;
        this.eventSubscriptionService = eventSubscriptionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEventList(
            Pageable pageable,
            Authentication authentication) {
        Long currentUserId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<EventResponse> list = eventService.getEventList(pageable, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = authentication != null ? (Long) authentication.getPrincipal() : null;
        EventResponse event = eventService.getEventById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime,
            @RequestParam("location") String location,
            @RequestParam(value = "organizer", required = false) String organizer,
            @RequestParam(value = "requirements", required = false) String requirements,
            @RequestParam(value = "maxParticipants", required = false) Integer maxParticipants,
            @RequestParam(value = "fee", required = false) BigDecimal fee,
            @RequestParam(value = "contactInfo", required = false) String contactInfo,
            @RequestParam(value = "registrationDeadline", required = false) LocalDateTime registrationDeadline,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Long currentUserId = (Long) authentication.getPrincipal();

        // 构建 EventRequest
        EventRequest request = new EventRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setType(type);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setLocation(location);
        request.setOrganizer(organizer);
        request.setRequirements(requirements);
        request.setMaxParticipants(maxParticipants);
        request.setFee(fee);
        request.setContactInfo(contactInfo);
        request.setRegistrationDeadline(registrationDeadline);
        request.setImages(images);

        EventResponse event = eventService.createEvent(request, currentUserId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("赛事创建成功", event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        EventResponse event = eventService.updateEvent(id, request, currentUserId, false);
        return ResponseEntity.ok(ApiResponse.success("赛事更新成功", event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("赛事删除成功", null));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelEvent(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        eventService.cancelEvent(id, currentUserId, false);
        return ResponseEntity.ok(ApiResponse.success("赛事已取消", null));
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribeEvent(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        eventSubscriptionService.subscribe(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("订阅成功", null));
    }

    @DeleteMapping("/{id}/subscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribeEvent(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        eventSubscriptionService.unsubscribe(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("取消订阅成功", null));
    }
}
