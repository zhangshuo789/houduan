package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 100, message = "标题5-100字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long boardId;
}
