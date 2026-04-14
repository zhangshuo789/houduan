package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowStatusResponse {
    private Boolean following;
    private Boolean followedBy;
    private Boolean mutualFollow;
}
