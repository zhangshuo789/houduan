package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchResultRequest {
    @NotNull(message = "胜者不能为空")
    private Long winnerId;
    private Integer score1;
    private Integer score2;
}
