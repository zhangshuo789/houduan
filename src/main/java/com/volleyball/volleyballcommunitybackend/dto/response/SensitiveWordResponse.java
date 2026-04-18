package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveWordResponse {
    private Long id;
    private String word;
    private String replacement;
    private String level;
    private LocalDateTime createdAt;
}
