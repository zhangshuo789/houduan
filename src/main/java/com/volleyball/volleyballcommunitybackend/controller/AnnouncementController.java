package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.AnnouncementRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.AnnouncementResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 公告列表（所有人可见，置顶优先）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getList(
            Pageable pageable, HttpServletRequest request) {
        Page<AnnouncementResponse> list = announcementService.getList(pageable, request);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getById(
            @PathVariable Long id, HttpServletRequest request) {
        AnnouncementResponse announcement = announcementService.getById(id, request);
        return ResponseEntity.ok(ApiResponse.success(announcement));
    }

    /**
     * 发布公告（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @Valid @RequestBody AnnouncementRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) authentication.getPrincipal();
        AnnouncementResponse announcement = announcementService.create(request, adminId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("公告发布成功", announcement));
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request,
            HttpServletRequest httpRequest) {
        AnnouncementResponse announcement = announcementService.update(id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("公告更新成功", announcement));
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("公告删除成功", null));
    }
}
