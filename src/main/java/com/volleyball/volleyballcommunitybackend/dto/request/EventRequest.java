package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventRequest {
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度需在2-100字符之间")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotBlank(message = "赛制不能为空")
    private String format;

    @NotNull(message = "参赛队伍数不能为空")
    @Min(value = 2, message = "参赛队伍数至少为2")
    private Integer bracketSize;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotBlank(message = "地点不能为空")
    private String location;

    private String organizer;
    private String requirements;
    private BigDecimal fee;
    private String contactInfo;
}
