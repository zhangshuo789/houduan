package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponse {
    private Long userId;
    private String nickname;
    private String avatar;
    private String role;
    private Boolean banned;
    private LocalDateTime joinedAt;
}
