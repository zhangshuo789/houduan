package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String organizer;
    private String requirements;
    private Integer maxParticipants;
    private BigDecimal fee;
    private String contactInfo;
    private LocalDateTime registrationDeadline;
    private List<String> imageUrls;
    private UserSimpleResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer registrationCount;
    private Integer subscriberCount;
    private Boolean isSubscribed;
    private Boolean hasRegistered;
}
