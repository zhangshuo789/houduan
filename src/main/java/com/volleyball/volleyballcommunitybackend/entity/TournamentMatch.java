package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_match")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "round", nullable = false)
    private Integer round;

    @Column(name = "match_order", nullable = false)
    private Integer matchOrder;

    @Column(name = "phase", nullable = false)
    private String phase = "KNOCKOUT";

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "team1_id")
    private Long team1Id;

    @Column(name = "team2_id")
    private Long team2Id;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "score1")
    private Integer score1;

    @Column(name = "score2")
    private Integer score2;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "next_match_id")
    private Long nextMatchId;

    @Column(name = "next_match_slot")
    private Integer nextMatchSlot;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
