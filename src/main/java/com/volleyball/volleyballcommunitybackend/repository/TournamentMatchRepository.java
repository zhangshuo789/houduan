package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.TournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {
    List<TournamentMatch> findByEventIdOrderByRoundAscMatchOrderAsc(Long eventId);
    List<TournamentMatch> findByEventIdAndPhaseOrderByRoundAscMatchOrderAsc(Long eventId, String phase);
    List<TournamentMatch> findByEventIdAndRound(Long eventId, Integer round);
    List<TournamentMatch> findByEventIdAndStatus(Long eventId, String status);
}
