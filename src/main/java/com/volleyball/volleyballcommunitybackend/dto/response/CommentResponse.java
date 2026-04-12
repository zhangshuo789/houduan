package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private UserInfo user;
    private Long parentId;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}
