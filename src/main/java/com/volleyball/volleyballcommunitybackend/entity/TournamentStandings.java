package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_standings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentStandings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "registration_id", nullable = false)
    private Long registrationId;

    @Column(name = "wins", nullable = false)
    private Integer wins = 0;

    @Column(name = "losses", nullable = false)
    private Integer losses = 0;

    @Column(name = "points_scored", nullable = false)
    private Integer pointsScored = 0;

    @Column(name = "points_lost", nullable = false)
    private Integer pointsLost = 0;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
