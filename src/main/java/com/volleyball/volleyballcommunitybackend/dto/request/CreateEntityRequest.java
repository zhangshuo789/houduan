package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class CreateEntityRequest {
    @NotBlank(message = "实体名称不能为空")
    private String name;

    @NotBlank(message = "实体类型不能为空，可选: PLAYER, TEAM, MATCH, TOURNAMENT")
    private String type;

    private String description;

    /** 类型特有属性，如 position, height, country 等 */
    private Map<String, Object> properties;
}
