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
    private Long userId;
    private String teamName;
    private Integer bracketPosition;
    private Boolean eliminated;
    private Boolean isChampion;
    private LocalDateTime createdAt;
}
