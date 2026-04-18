package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime createdAt;
}
