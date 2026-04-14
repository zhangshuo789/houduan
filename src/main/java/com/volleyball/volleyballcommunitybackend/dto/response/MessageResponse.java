package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private String type;
    private Long targetId;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
