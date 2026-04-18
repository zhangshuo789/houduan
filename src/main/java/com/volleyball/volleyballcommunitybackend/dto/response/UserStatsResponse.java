package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {
    private long totalUsers;
    private long newUsersToday;
    private long newUsersThisMonth;
    private long activeUsersToday;
    private long activeUsersThisMonth;
}
