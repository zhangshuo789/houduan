package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BracketResponse {
    private Long eventId;
    private String format;
    private Integer bracketSize;
    private Integer registeredCount;
    private String eventStatus;
    private List<RoundData> rounds;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoundData {
        private Integer round;
        private String phase;
        private String groupName;
        private List<MatchData> matches;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchData {
        private Long matchId;
        private Integer matchOrder;
        private TeamSlot team1;
        private TeamSlot team2;
        private Long winnerId;
        private Integer score1;
        private Integer score2;
        private String status;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamSlot {
        private Long registrationId;
        private String teamName;
        private Integer bracketPosition;
        private Boolean eliminated;
        private Boolean isChampion;
    }
}
