package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import com.volleyball.volleyballcommunitybackend.repository.EventRegistrationRepository;
import com.volleyball.volleyballcommunitybackend.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public AdminEventService(EventRepository eventRepository,
                             EventRegistrationRepository eventRegistrationRepository) {
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    // 获取所有赛事列表
    public Page<Event> getEventList(Pageable pageable) {
        return eventRepository.findAllByOrderByStartTimeAsc(pageable);
    }

    // 修改赛事状态
    @Transactional
    public void updateEventStatus(Long eventId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        event.setStatus(status);
        eventRepository.save(event);
    }

    // 获取赛事报名列表
    public Page<EventRegistration> getEventRegistrations(Long eventId, Pageable pageable) {
        return eventRegistrationRepository.findByEventId(eventId, pageable);
    }
}
