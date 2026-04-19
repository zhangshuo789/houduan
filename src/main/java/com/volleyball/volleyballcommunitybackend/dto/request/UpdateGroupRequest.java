package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateGroupRequest {
    @Size(max = 50, message = "群名称不能超过50字符")
    private String name;

    @Size(max = 255, message = "群描述不能超过255字符")
    private String description;
}
