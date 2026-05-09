package com.volleyball.volleyballcommunitybackend.scheduler;

import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.repository.EventRepository;
import com.volleyball.volleyballcommunitybackend.service.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventStatusScheduler.class);

    private final EventRepository eventRepository;
    private final TournamentService tournamentService;

    public EventStatusScheduler(EventRepository eventRepository, TournamentService tournamentService) {
        this.eventRepository = eventRepository;
        this.tournamentService = tournamentService;
    }

    /**
     * 每分钟扫描一次：自动开赛
     * 状态为 REGISTERING 且 startTime <= now 的赛事自动开赛
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoStartEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByStatusAndStartTimeBefore("REGISTERING", now);

        for (Event event : events) {
            try {
                log.info("自动开赛: {} (ID: {})", event.getTitle(), event.getId());
                tournamentService.startEvent(event.getId());
                log.info("赛事已自动开赛: {} (ID: {})", event.getTitle(), event.getId());
            } catch (Exception e) {
                log.error("自动开赛失败: {} (ID: {}), error: {}", event.getTitle(), event.getId(), e.getMessage());
            }
        }
    }
}
