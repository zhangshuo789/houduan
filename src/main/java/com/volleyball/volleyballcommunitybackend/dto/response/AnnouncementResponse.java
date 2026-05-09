package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private Boolean pinned;
    private UserSimpleResponse publishedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
