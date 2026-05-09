package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRegistrationRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import com.volleyball.volleyballcommunitybackend.repository.EventRegistrationRepository;
import com.volleyball.volleyballcommunitybackend.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final TournamentService tournamentService;

    public EventRegistrationService(EventRegistrationRepository registrationRepository,
                                    EventRepository eventRepository,
                                    TournamentService tournamentService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.tournamentService = tournamentService;
    }

    @Transactional
    public EventRegistrationResponse register(Long eventId, Long userId, EventRegistrationRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        if (!"REGISTERING".equals(event.getStatus())) {
            throw new RuntimeException("赛事当前不在报名阶段");
        }

        if (registrationRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("已报名该赛事");
        }

        long currentCount = registrationRepository.countByEventId(eventId);
        if (currentCount >= event.getBracketSize()) {
            throw new RuntimeException("报名已满");
        }

        // 分配 bracket 位置
        int position = tournamentService.assignBracketPosition(eventId, event.getBracketSize());

        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserId(userId);
        registration.setTeamName(request.getTeamName());
        registration.setBracketPosition(position);
        registration.setEliminated(false);
        registration.setIsChampion(false);

        EventRegistration saved = registrationRepository.save(registration);

        // 实时更新首轮对阵图
        tournamentService.ensureFirstRoundMatch(eventId, event.getBracketSize(), position, saved.getId());

        return toResponse(saved);
    }

    public Page<EventRegistrationResponse> getRegistrations(Long eventId, Pageable pageable) {
        return registrationRepository.findByEventId(eventId, pageable)
                .map(this::toResponse);
    }

    private EventRegistrationResponse toResponse(EventRegistration r) {
        return new EventRegistrationResponse(
                r.getId(),
                r.getEventId(),
                r.getUserId(),
                r.getTeamName(),
                r.getBracketPosition(),
                r.getEliminated(),
                r.getIsChampion(),
                r.getCreatedAt()
        );
    }
}
