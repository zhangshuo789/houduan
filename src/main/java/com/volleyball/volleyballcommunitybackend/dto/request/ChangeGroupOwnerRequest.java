package com.volleyball.volleyballcommunitybackend.dto.request;

import lombok.Data;

@Data
public class ChangeGroupOwnerRequest {
    private Long newOwnerId;
}
