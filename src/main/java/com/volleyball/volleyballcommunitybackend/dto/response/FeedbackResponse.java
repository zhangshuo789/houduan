package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String title;
    private String content;
    private String category;
    private String status;
    private String reply;
    private UserSimpleResponse repliedBy;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
