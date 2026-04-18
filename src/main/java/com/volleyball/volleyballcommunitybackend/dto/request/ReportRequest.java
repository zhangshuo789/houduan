package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotBlank(message = "被举报类型不能为空")
    private String targetType;  // POST, COMMENT, EVENT

    @NotNull(message = "被举报内容ID不能为空")
    private Long targetId;

    @NotBlank(message = "举报原因不能为空")
    private String reason;
}
