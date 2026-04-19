package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class GroupRequest {
    @NotBlank(message = "群名称不能为空")
    private String name;

    private String description;

    // 初始成员可以为空，创建者自动成为群主，后续再邀请成员
    private List<Long> memberIds;
}
