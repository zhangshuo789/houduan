package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    // 通知类型: BROADCAST(广播) 或 PRIVATE(私信)
    private String type = "BROADCAST";

    // 目标用户ID，type=PRIVATE时必填，type=BROADCAST时忽略
    private Long targetUserId;

    // 是否存入数据库（仅对BROADCAST有效，PRIVATE总是存入）
    // true: 存入数据库+SSE广播，false: 仅SSE广播不存
    private Boolean persist = true;
}
