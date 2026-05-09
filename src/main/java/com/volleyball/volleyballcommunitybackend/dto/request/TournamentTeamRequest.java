package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TournamentTeamRequest {
    @NotBlank(message = "队伍名称不能为空")
    private String teamName;
}
