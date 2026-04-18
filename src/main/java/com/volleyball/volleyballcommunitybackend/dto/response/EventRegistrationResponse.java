package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationResponse {
    private Long id;
    private Long eventId;
    private String teamName;
    private String contactPerson;
    private String contactPhone; // 脱敏
    private Integer teamSize;
    private String status;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
