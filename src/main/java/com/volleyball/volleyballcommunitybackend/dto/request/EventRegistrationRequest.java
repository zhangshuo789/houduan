package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRegistrationRequest {
    @NotBlank(message = "球队名称不能为空")
    private String teamName;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系方式不能为空")
    private String contactPhone;

    @NotNull(message = "参赛人数不能为空")
    @Min(value = 1, message = "参赛人数至少为1")
    private Integer teamSize;
}
