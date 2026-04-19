package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupListResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private Integer memberCount;
    private LocalDateTime createdAt;
}
