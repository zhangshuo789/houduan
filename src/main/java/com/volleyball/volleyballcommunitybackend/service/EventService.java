package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.EventResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserSimpleResponse;
import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventImage;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public EventService(EventRepository eventRepository, EventImageRepository eventImageRepository,
                        EventRegistrationRepository registrationRepository,
                        EventSubscriptionRepository subscriptionRepository,
                        UserRepository userRepository, FileService fileService) {
        this.eventRepository = eventRepository;
        this.eventImageRepository = eventImageRepository;
        this.registrationRepository = registrationRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public Page<EventResponse> getEventList(Pageable pageable, Long currentUserId) {
        return eventRepository.findAllByOrderByStartTimeAsc(pageable)
                .map(event -> toEventResponse(event, null, currentUserId));
    }

    public EventResponse getEventById(Long id, HttpServletRequest request, Long currentUserId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        return toEventResponse(event, request, currentUserId);
    }

    @Transactional
    public EventResponse createEvent(EventRequest request, Long userId, HttpServletRequest httpRequest) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setFormat(request.getFormat());
        event.setBracketSize(request.getBracketSize());
        event.setStatus("REGISTERING");
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setOrganizer(request.getOrganizer());
        event.setRequirements(request.getRequirements());
        event.setFee(request.getFee());
        event.setContactInfo(request.getContactInfo());
        event.setCreatedBy(userId);

        Event saved = eventRepository.save(event);
        return toEventResponse(saved, httpRequest, userId);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, Long userId, boolean isAdmin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        if (!isAdmin && !event.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权修改此赛事");
        }

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getType() != null) event.setType(request.getType());
        if (request.getFormat() != null) event.setFormat(request.getFormat());
        if (request.getBracketSize() != null) event.setBracketSize(request.getBracketSize());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getOrganizer() != null) event.setOrganizer(request.getOrganizer());
        if (request.getRequirements() != null) event.setRequirements(request.getRequirements());
        if (request.getFee() != null) event.setFee(request.getFee());
        if (request.getContactInfo() != null) event.setContactInfo(request.getContactInfo());

        Event saved = eventRepository.save(event);
        return toEventResponse(saved, null, userId);
    }

    @Transactional
    public void deleteEvent(Long id, Long userId, boolean isAdmin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        if (!isAdmin && !event.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权删除此赛事");
        }
        eventRepository.delete(event);
    }

    public boolean isEventOrganizer(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        return event.getCreatedBy().equals(userId);
    }

    private EventResponse toEventResponse(Event event, HttpServletRequest request, Long currentUserId) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setType(event.getType());
        response.setStatus(event.getStatus());
        response.setFormat(event.getFormat());
        response.setBracketSize(event.getBracketSize());
        response.setCurrentRound(event.getCurrentRound());
        response.setStartTime(event.getStartTime());
        response.setEndTime(event.getEndTime());
        response.setLocation(event.getLocation());
        response.setOrganizer(event.getOrganizer());
        response.setRequirements(event.getRequirements());
        response.setFee(event.getFee());
        response.setContactInfo(event.getContactInfo());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());

        // 创建者信息
        User creator = userRepository.findById(event.getCreatedBy()).orElse(null);
        if (creator != null) {
            response.setCreatedBy(new UserSimpleResponse(
                    creator.getId(),
                    creator.getNickname(),
                    getAvatarUrl(creator.getAvatar(), request)
            ));
        }

        // 图片
        List<String> imageUrls = eventImageRepository.findByEventIdOrderBySortOrderAsc(event.getId())
                .stream()
                .map(EventImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);

        // 报名数
        long count = registrationRepository.countByEventId(event.getId());
        response.setRegistrationCount((int) count);

        // 当前用户是否已报名
        if (currentUserId != null) {
            response.setHasRegistered(registrationRepository.existsByEventIdAndUserId(event.getId(), currentUserId));
            response.setIsSubscribed(subscriptionRepository.existsByEventIdAndUserId(event.getId(), currentUserId));
        } else {
            response.setHasRegistered(false);
            response.setIsSubscribed(false);
        }

        // 订阅数
        long subCount = subscriptionRepository.countByEventId(event.getId());
        response.setSubscriberCount((int) subCount);

        return response;
    }

    private String getAvatarUrl(String avatar, HttpServletRequest request) {
        if (avatar == null || avatar.isEmpty() || request == null) return null;
        try {
            Long fileId = Long.parseLong(avatar);
            return fileService.getFileUrl(fileId, request);
        } catch (NumberFormatException e) {
            return avatar;
        }
    }
}
