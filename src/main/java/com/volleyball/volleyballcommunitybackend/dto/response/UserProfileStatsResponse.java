package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileStatsResponse {
    private Long followCount;
    private Long followerCount;
    private Long postCount;
    private Long friendCount;
}
