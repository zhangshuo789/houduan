package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventImage;
import com.volleyball.volleyballcommunitybackend.repository.EventRepository;
import com.volleyball.volleyballcommunitybackend.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/event/{eventId}/images")
public class EventImageController {

    private final FileService fileService;
    private final EventRepository eventRepository;

    public EventImageController(FileService fileService, EventRepository eventRepository) {
        this.fileService = fileService;
        this.eventRepository = eventRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @PathVariable Long eventId,
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication,
            HttpServletRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        List<String> imageUrls = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            // 使用 post_image 类型上传
            var fileResponse = fileService.uploadFile(file, "post_image",
                    event.getCreatedBy(), request);
            imageUrls.add(fileResponse.getUrl());
        }

        return ResponseEntity.ok(ApiResponse.success("图片上传成功", imageUrls));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getImages(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        List<String> imageUrls = event.getImages().stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(imageUrls));
    }
}
