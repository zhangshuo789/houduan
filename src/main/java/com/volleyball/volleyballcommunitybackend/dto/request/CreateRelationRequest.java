package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class CreateRelationRequest {
    @NotBlank(message = "源实体 ID 不能为空")
    private String fromId;

    @NotBlank(message = "目标实体 ID 不能为空")
    private String toId;

    @NotBlank(message = "关系类型不能为空，可选: PLAYS_FOR, PARTICIPATES_IN, BELONGS_TO, COACHES, TEAMMATE_OF")
    private String relationType;

    /** 关系属性，如 since, until, number 等 */
    private Map<String, Object> properties;
}
