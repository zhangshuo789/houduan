package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetUserStatusRequest {
    @NotNull(message = "禁用状态不能为空")
    private Boolean disabled;

    private String reason;
}
