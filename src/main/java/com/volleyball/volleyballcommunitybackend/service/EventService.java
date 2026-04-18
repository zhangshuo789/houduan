package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.EventResponse;
import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventImage;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final EventSubscriptionRepository eventSubscriptionRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public EventService(EventRepository eventRepository, EventImageRepository eventImageRepository,
                        EventSubscriptionRepository eventSubscriptionRepository,
                        EventRegistrationRepository eventRegistrationRepository,
                        UserRepository userRepository, FileService fileService) {
        this.eventRepository = eventRepository;
        this.eventImageRepository = eventImageRepository;
        this.eventSubscriptionRepository = eventSubscriptionRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public Page<EventResponse> getEventList(Pageable pageable, Long currentUserId) {
        return eventRepository.findAllByOrderByStartTimeAsc(pageable)
                .map(event -> toEventResponse(event, currentUserId, null));
    }

    public EventResponse getEventById(Long id, Long currentUserId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        return toEventResponse(event, currentUserId, null);
    }

    @Transactional
    public EventResponse createEvent(EventRequest request, Long userId, HttpServletRequest httpRequest) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setStatus("REGISTERING");
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setOrganizer(request.getOrganizer());
        event.setRequirements(request.getRequirements());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setFee(request.getFee());
        event.setContactInfo(request.getContactInfo());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setCreatedBy(userId);

        Event saved = eventRepository.save(event);
        return toEventResponse(saved, userId, httpRequest);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, Long userId, boolean isAdmin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        if (!isAdmin && !event.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权修改此赛事");
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            event.setType(request.getType());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getOrganizer() != null) {
            event.setOrganizer(request.getOrganizer());
        }
        if (request.getRequirements() != null) {
            event.setRequirements(request.getRequirements());
        }
        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getFee() != null) {
            event.setFee(request.getFee());
        }
        if (request.getContactInfo() != null) {
            event.setContactInfo(request.getContactInfo());
        }
        if (request.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(request.getRegistrationDeadline());
        }

        Event saved = eventRepository.save(event);
        return toEventResponse(saved, userId, null);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        eventRepository.delete(event);
    }

    @Transactional
    public void cancelEvent(Long id, Long userId, boolean isAdmin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        // 非管理员只能取消自己的赛事
        if (!isAdmin && !event.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权取消此赛事");
        }

        // 已结束或已取消的赛事不能再取消
        if ("ENDED".equals(event.getStatus()) || "CANCELLED".equals(event.getStatus())) {
            throw new RuntimeException("该赛事已结束或已取消");
        }

        event.setStatus("CANCELLED");
        eventRepository.save(event);
    }

    public Page<EventResponse> getSubscriptionsByUserId(Long userId, Pageable pageable, HttpServletRequest request) {
        return eventSubscriptionRepository.findByUserId(userId, pageable)
                .map(subscription -> {
                    Event event = eventRepository.findById(subscription.getEventId())
                            .orElseThrow(() -> new RuntimeException("赛事不存在"));
                    return toEventResponse(event, userId, request);
                });
    }

    public boolean isEventOrganizer(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        return event.getCreatedBy().equals(userId);
    }

    private EventResponse toEventResponse(Event event, Long currentUserId, HttpServletRequest request) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setType(event.getType());
        response.setStatus(event.getStatus());
        response.setStartTime(event.getStartTime());
        response.setEndTime(event.getEndTime());
        response.setLocation(event.getLocation());
        response.setOrganizer(event.getOrganizer());
        response.setRequirements(event.getRequirements());
        response.setMaxParticipants(event.getMaxParticipants());
        response.setFee(event.getFee());
        response.setContactInfo(event.getContactInfo());
        response.setRegistrationDeadline(event.getRegistrationDeadline());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());

        // 获取创建者信息
        User creator = userRepository.findById(event.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        response.setCreatedBy(new com.volleyball.volleyballcommunitybackend.dto.response.UserSimpleResponse(
                creator.getId(),
                creator.getNickname(),
                getAvatarUrl(creator.getAvatar(), request)
        ));

        // 获取图片URL列表
        List<String> imageUrls = eventImageRepository.findByEventIdOrderBySortOrderAsc(event.getId())
                .stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);

        // 获取订阅和报名数量
        long subscriptionCount = eventSubscriptionRepository.countByEventId(event.getId());
        long registrationCount = eventRegistrationRepository.countByEventId(event.getId());
        response.setSubscriberCount((int) subscriptionCount);
        response.setRegistrationCount((int) registrationCount);

        // 检查当前用户是否订阅和报名
        if (currentUserId != null) {
            response.setIsSubscribed(eventSubscriptionRepository.existsByEventIdAndUserId(event.getId(), currentUserId));
            response.setHasRegistered(eventRegistrationRepository.existsByEventIdAndUserId(event.getId(), currentUserId));
        } else {
            response.setIsSubscribed(false);
            response.setHasRegistered(false);
        }

        return response;
    }

    private String getAvatarUrl(String avatar, HttpServletRequest request) {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        try {
            Long fileId = Long.parseLong(avatar);
            if (request == null) {
                return null;
            }
            return fileService.getFileUrl(fileId, request);
        } catch (NumberFormatException e) {
            return avatar;
        }
    }
}
