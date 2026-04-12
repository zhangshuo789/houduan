package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
}
