package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.TournamentStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentStandingsRepository extends JpaRepository<TournamentStandings, Long> {
    List<TournamentStandings> findByEventIdAndGroupNameOrderByRankAsc(Long eventId, String groupName);
    List<TournamentStandings> findByEventIdOrderByGroupNameAscRankAsc(Long eventId);
}
