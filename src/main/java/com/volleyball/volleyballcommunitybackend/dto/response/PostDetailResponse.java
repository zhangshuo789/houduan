package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private PostResponse.UserInfo user;
    private PostResponse.BoardInfo board;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private Boolean liked;
    private Boolean favorited;
}
