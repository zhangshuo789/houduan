package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class GroupRequest {
    @NotBlank(message = "群名称不能为空")
    private String name;

    private String description;

    @NotEmpty(message = "至少需要1个成员")
    private List<Long> memberIds;
}
