package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventRequest {
    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度需在5-100字符之间")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "类型不能为空")
    private String type; // MATCH or ACTIVITY

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotBlank(message = "地点不能为空")
    private String location;

    private String organizer;
    private String requirements;
    private Integer maxParticipants;
    private BigDecimal fee;
    private String contactInfo;
    private LocalDateTime registrationDeadline;
    private List<String> imageUrls;
}
