package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedResponse {
    private Long postId;
    private String title;
    private Long boardId;
    private String boardName;
    private UserResponse user;
    private LocalDateTime createdAt;
}
