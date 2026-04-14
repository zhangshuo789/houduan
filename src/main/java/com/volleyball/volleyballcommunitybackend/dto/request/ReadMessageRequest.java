package com.volleyball.volleyballcommunitybackend.dto.request;

import lombok.Data;

@Data
public class ReadMessageRequest {
    private Long conversationWithUserId;
    private Long groupId;
}
