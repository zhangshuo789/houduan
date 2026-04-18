package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsOverviewResponse {
    private long totalUsers;
    private long totalPosts;
    private long totalComments;
    private long totalEvents;
    private long totalGroups;
    private long totalReports;
    private long pendingReports;
    private long disabledUsers;
}
