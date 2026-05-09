package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Boolean pinned;
}
