package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentStatsResponse {
    private long totalPosts;
    private long totalComments;
    private long postsToday;
    private long commentsToday;
    private long postsThisMonth;
    private long commentsThisMonth;
}
