package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.service.SseService;
import com.volleyball.volleyballcommunitybackend.util.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;
    private final JwtUtil jwtUtil;

    public SseController(SseService sseService, JwtUtil jwtUtil) {
        this.sseService = sseService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam("token") String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("无效的Token");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        return sseService.connect(userId);
    }
}
