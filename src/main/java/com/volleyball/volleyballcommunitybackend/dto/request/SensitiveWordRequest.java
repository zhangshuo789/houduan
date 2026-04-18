package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SensitiveWordRequest {
    @NotBlank(message = "敏感词不能为空")
    @Size(max = 50, message = "敏感词最多50字符")
    private String word;

    @Size(max = 50, message = "替换词最多50字符")
    private String replacement = "***";

    private String level = "WARN";  // WARN, BLOCK
}
