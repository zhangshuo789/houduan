package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserManagementResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private List<String> roles;
    private Boolean disabled;
    private String disabledReason;
    private LocalDateTime createdAt;
}
