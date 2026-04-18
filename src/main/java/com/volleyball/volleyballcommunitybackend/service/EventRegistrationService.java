package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRegistrationRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import com.volleyball.volleyballcommunitybackend.repository.EventRegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventRegistrationService {

    private final EventRegistrationRepository eventRegistrationRepository;

    public EventRegistrationService(EventRegistrationRepository eventRegistrationRepository) {
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    @Transactional
    public EventRegistrationResponse register(Long eventId, Long userId, EventRegistrationRequest request) {
        if (eventRegistrationRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("已报名该赛事");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserId(userId);
        registration.setTeamName(request.getTeamName());
        registration.setContactPerson(request.getContactPerson());
        registration.setContactPhone(request.getContactPhone());
        registration.setTeamSize(request.getTeamSize());
        registration.setStatus("PENDING");

        EventRegistration saved = eventRegistrationRepository.save(registration);
        return toEventRegistrationResponse(saved);
    }

    public Page<EventRegistrationResponse> getRegistrations(Long eventId, Pageable pageable) {
        return eventRegistrationRepository.findByEventId(eventId, pageable)
                .map(this::toEventRegistrationResponse);
    }

    @Transactional
    public EventRegistrationResponse approveRegistration(Long registrationId, Long reviewerId, boolean approved) {
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));

        registration.setReviewedBy(reviewerId);
        registration.setReviewedAt(LocalDateTime.now());
        registration.setStatus(approved ? "APPROVED" : "REJECTED");

        EventRegistration saved = eventRegistrationRepository.save(registration);
        return toEventRegistrationResponse(saved);
    }

    private EventRegistrationResponse toEventRegistrationResponse(EventRegistration registration) {
        return new EventRegistrationResponse(
                registration.getId(),
                registration.getEventId(),
                registration.getTeamName(),
                registration.getContactPerson(),
                maskPhone(registration.getContactPhone()),
                registration.getTeamSize(),
                registration.getStatus(),
                registration.getReviewedAt(),
                registration.getCreatedAt()
        );
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
