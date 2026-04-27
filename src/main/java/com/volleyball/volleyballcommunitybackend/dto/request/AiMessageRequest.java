package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiMessageRequest {
    @NotBlank(message = "消息内容不能为空")
    private String content;

    private Boolean thinking = false;

    private Boolean stream = false;
}