package com.volleyball.volleyballcommunitybackend.scheduler;

import com.volleyball.volleyballcommunitybackend.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class SseTestScheduler {

    private static final Logger log = LoggerFactory.getLogger(SseTestScheduler.class);

    private final SseService sseService;

    public SseTestScheduler(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * 测试 SSE 推送 - 每20秒向用户ID=1发送一条测试消息
     */
    @Scheduled(fixedRate = 20000) // 每20秒执行
    public void sendTestNotification() {
        Long testUserId = 1L;  // 测试用户ID
        String message = "测试消息 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        Map<String, Object> data = Map.of(
                "type", "TEST",
                "content", message,
                "time", LocalDateTime.now().toString()
        );

        try {
            sseService.sendEventStatusChanged(testUserId, data);
            log.info("SSE测试消息已发送: {} -> {}", testUserId, message);
        } catch (Exception e) {
            log.warn("SSE测试消息发送失败: {}", e.getMessage());
        }
    }
}
