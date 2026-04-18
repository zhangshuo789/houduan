package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetRoleRequest {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
