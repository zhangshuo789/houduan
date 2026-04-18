package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private String targetType;
    private Long targetId;
    private String targetTitle;  // 可选：帖子标题等
    private String reason;
    private String status;
    private Long handledBy;
    private String handledByNickname;
    private LocalDateTime handledAt;
    private String handleResult;
    private LocalDateTime createdAt;
}
