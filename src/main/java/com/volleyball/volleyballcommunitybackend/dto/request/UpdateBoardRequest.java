package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBoardRequest {
    @Size(max = 50, message = "板块名称最多50字符")
    private String name;

    @Size(max = 255, message = "板块描述最多255字符")
    private String description;

    @Size(max = 50, message = "图标最多50字符")
    private String icon;
}
