package com.volleyball.volleyballcommunitybackend.scheduler;

import com.volleyball.volleyballcommunitybackend.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class SseTestScheduler {

    private static final Logger log = LoggerFactory.getLogger(SseTestScheduler.class);

    private final SseService sseService;

    public SseTestScheduler(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * 测试 SSE 广播 - 每20秒向所有已连接用户发送赛事通知格式的测试消息
     */
    @Scheduled(fixedRate = 20000) // 每20秒执行
    public void sendTestNotification() {
        String message = "测试赛事通知 " + LocalDateTime.now();

        Map<String, Object> data = Map.of(
                "eventId", 999L,
                "eventTitle", "测试赛事",
                "oldStatus", "REGISTERING",
                "newStatus", "IN_PROGRESS",
                "message", message
        );

        try {
            sseService.broadcast("eventStatusChanged", data);
            log.info("SSE测试广播已发送: {}", message);
        } catch (Exception e) {
            log.warn("SSE测试广播发送失败: {}", e.getMessage());
        }
    }
}
