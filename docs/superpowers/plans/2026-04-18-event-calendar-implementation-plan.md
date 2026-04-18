# 赛事日历功能实施计划 (Phase 4)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现赛事日历功能，包括赛事发布/订阅/报名，以及 SSE 实时推送

**Architecture:** 遵循现有项目模式：Entity + Repository + Service + Controller，赛事类型使用 String 字段而非 Java enum

**Tech Stack:** Spring Boot 4.0.5, Java 17, JPA, SSE

---

## 文件结构

新增文件：
```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── entity/
│   ├── Event.java              # 赛事实体
│   ├── EventImage.java         # 赛事图片实体
│   ├── EventSubscription.java  # 赛事订阅实体
│   └── EventRegistration.java  # 赛事报名实体
├── repository/
│   ├── EventRepository.java
│   ├── EventImageRepository.java
│   ├── EventSubscriptionRepository.java
│   └── EventRegistrationRepository.java
├── service/
│   ├── EventService.java
│   ├── EventSubscriptionService.java
│   └── EventRegistrationService.java
├── controller/
│   ├── EventController.java
│   └── EventRegistrationController.java
└── dto/
    ├── request/
    │   ├── EventRequest.java
    │   └── EventRegistrationRequest.java
    └── response/
        ├── EventResponse.java
        ├── EventListResponse.java
        └── EventRegistrationResponse.java
```

修改文件：
```
src/main/java/com/volleyball/volleyballcommunitybackend/config/SecurityConfig.java
docs/api.md
docs/project.md
```

---

## Task 1: 创建实体类

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/Event.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/EventImage.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/EventSubscription.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/EventRegistration.java`

- [ ] **Step 1: 创建 Event.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String type; // MATCH:比赛, ACTIVITY:活动

    @Column(nullable = false, length = 20)
    private String status = "PREPARING"; // PREPARING, REGISTERING, IN_PROGRESS, ENDED, CANCELLED

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String location;

    @Column(length = 100)
    private String organizer;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> images;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: 创建 EventImage.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "event_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
```

- [ ] **Step 3: 创建 EventSubscription.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_subscription", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 4: 创建 EventRegistration.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Column(name = "contact_person", nullable = false, length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "team_size", nullable = false)
    private Integer teamSize;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/entity/
git commit -m "feat: add event calendar entity classes"
```

---

## Task 2: 创建 Repository

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/EventRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/EventImageRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/EventSubscriptionRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/EventRegistrationRepository.java`

- [ ] **Step 1: 创建 EventRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByOrderByStartTimeAsc(Pageable pageable);
}
```

- [ ] **Step 2: 创建 EventImageRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    List<EventImage> findByEventIdOrderBySortOrderAsc(Long eventId);

    void deleteByEventId(Long eventId);
}
```

- [ ] **Step 3: 创建 EventSubscriptionRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.EventSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    void deleteByEventIdAndUserId(Long eventId, Long userId);

    Page<EventSubscription> findByUserId(Long userId, Pageable pageable);

    long countByEventId(Long eventId);
}
```

- [ ] **Step 4: 创建 EventRegistrationRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);

    Page<EventRegistration> findByEventIdAndStatus(Long eventId, String status, Pageable pageable);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    long countByEventId(Long eventId);

    long countByEventIdAndStatus(Long eventId, String status);
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/
git commit -m "feat: add event calendar repository classes"
```

---

## Task 3: 创建 DTO 类

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/EventRequest.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/EventRegistrationRequest.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/EventResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/EventRegistrationResponse.java`

- [ ] **Step 1: 创建 EventRequest.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度需在5-100字符之间")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "类型不能为空")
    private String type; // MATCH or ACTIVITY

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotBlank(message = "地点不能为空")
    private String location;

    private String organizer;

    private String requirements;

    private Integer maxParticipants;

    private BigDecimal fee;

    private String contactInfo;

    private LocalDateTime registrationDeadline;

    private List<String> imageUrls;
}
```

- [ ] **Step 2: 创建 EventRegistrationRequest.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class EventRegistrationRequest {

    @NotBlank(message = "球队名称不能为空")
    private String teamName;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系方式不能为空")
    private String contactPhone;

    @NotNull(message = "参赛人数不能为空")
    @Min(value = 1, message = "参赛人数至少为1")
    private Integer teamSize;
}
```

- [ ] **Step 3: 创建 EventResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String organizer;
    private String requirements;
    private Integer maxParticipants;
    private BigDecimal fee;
    private String contactInfo;
    private LocalDateTime registrationDeadline;
    private List<String> imageUrls;
    private UserSimpleResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer registrationCount;
    private Integer subscriberCount;
    private Boolean isSubscribed;
    private Boolean hasRegistered;
}
```

- [ ] **Step 4: 创建 UserSimpleResponse.java** (用于 EventResponse.createdBy)

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSimpleResponse {
    private Long id;
    private String nickname;
    private String avatar;
}
```

- [ ] **Step 5: 创建 EventRegistrationResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationResponse {
    private Long id;
    private Long eventId;
    private String teamName;
    private String contactPerson;
    private String contactPhone; // 脱敏
    private Integer teamSize;
    private String status;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/
git commit -m "feat: add event calendar DTO classes"
```

---

## Task 4: 创建 Service 类

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/EventService.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/EventSubscriptionService.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/EventRegistrationService.java`

- [ ] **Step 1: 创建 EventService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserSimpleResponse;
import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventImage;
import com.volleyball.volleyballcommunitybackend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final EventSubscriptionRepository subscriptionRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public EventService(EventRepository eventRepository, EventImageRepository eventImageRepository,
                       EventSubscriptionRepository subscriptionRepository,
                       EventRegistrationRepository registrationRepository,
                       UserRepository userRepository, FileService fileService) {
        this.eventRepository = eventRepository;
        this.eventImageRepository = eventImageRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public Page<EventResponse> getEventList(Pageable pageable, Long currentUserId) {
        return eventRepository.findAllByOrderByStartTimeAsc(pageable)
                .map(event -> toEventResponse(event, currentUserId, false, null));
    }

    public EventResponse getEventById(Long id, Long currentUserId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        return toEventResponse(event, currentUserId, true, null);
    }

    @Transactional
    public EventResponse createEvent(EventRequest request, Long userId) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setStatus("PREPARING");
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

        // 保存图片
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                EventImage image = new EventImage();
                image.setEvent(saved);
                image.setImageUrl(request.getImageUrls().get(i));
                image.setSortOrder(i);
                eventImageRepository.save(image);
            }
        }

        return toEventResponse(saved, userId, true, null);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, Long userId, boolean isAdmin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        if (!isAdmin && !event.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权限修改此赛事");
        }

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getOrganizer() != null) event.setOrganizer(request.getOrganizer());
        if (request.getRequirements() != null) event.setRequirements(request.getRequirements());
        if (request.getMaxParticipants() != null) event.setMaxParticipants(request.getMaxParticipants());
        if (request.getFee() != null) event.setFee(request.getFee());
        if (request.getContactInfo() != null) event.setContactInfo(request.getContactInfo());
        if (request.getRegistrationDeadline() != null) event.setRegistrationDeadline(request.getRegistrationDeadline());
        if (request.getStatus() != null && isAdmin) event.setStatus(request.getStatus());

        Event saved = eventRepository.save(event);

        // 更新图片
        if (request.getImageUrls() != null) {
            eventImageRepository.deleteByEventId(id);
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                EventImage image = new EventImage();
                image.setEvent(saved);
                image.setImageUrl(request.getImageUrls().get(i));
                image.setSortOrder(i);
                eventImageRepository.save(image);
            }
        }

        return toEventResponse(saved, userId, true, null);
    }

    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    public Page<EventResponse> getSubscriptionsByUserId(Long userId, Pageable pageable, HttpServletRequest request) {
        return subscriptionRepository.findByUserId(userId, pageable)
                .map(sub -> {
                    Event event = eventRepository.findById(sub.getEventId())
                            .orElseThrow(() -> new RuntimeException("赛事不存在"));
                    return toEventResponse(event, userId, false, request);
                });
    }

    public boolean isEventOrganizer(Long eventId, Long userId) {
        return eventRepository.findById(eventId)
                .map(event -> event.getCreatedBy().equals(userId))
                .orElse(false);
    }

    private EventResponse toEventResponse(Event event, Long currentUserId, boolean includeDetails, HttpServletRequest request) {
        List<String> imageUrls = eventImageRepository.findByEventIdOrderBySortOrderAsc(event.getId())
                .stream().map(EventImage::getImageUrl).collect(Collectors.toList());

        UserSimpleResponse creator = userRepository.findById(event.getCreatedBy())
                .map(u -> new UserSimpleResponse(u.getId(), u.getNickname(), getAvatarUrl(u.getAvatar(), request)))
                .orElse(new UserSimpleResponse(event.getCreatedBy(), "未知用户", null));

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
        response.setImageUrls(imageUrls);
        response.setCreatedBy(creator);
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        response.setRegistrationCount((int) registrationRepository.countByEventId(event.getId()));
        response.setSubscriberCount((int) subscriptionRepository.countByEventId(event.getId()));

        if (currentUserId != null) {
            response.setIsSubscribed(subscriptionRepository.existsByEventIdAndUserId(event.getId(), currentUserId));
            response.setHasRegistered(registrationRepository.existsByEventIdAndUserId(event.getId(), currentUserId));
        }

        return response;
    }

    private String getAvatarUrl(String avatar, HttpServletRequest request) {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        try {
            Long fileId = Long.parseLong(avatar);
            return fileService.getFileUrl(fileId, request);
        } catch (NumberFormatException e) {
            return avatar;
        }
    }
}
```

- [ ] **Step 2: 创建 EventSubscriptionService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.EventSubscription;
import com.volleyball.volleyballcommunitybackend.repository.EventSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventSubscriptionService {

    private final EventSubscriptionRepository subscriptionRepository;

    public EventSubscriptionService(EventSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public void subscribe(Long eventId, Long userId) {
        if (subscriptionRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("已订阅过此赛事");
        }
        EventSubscription subscription = new EventSubscription();
        subscription.setEventId(eventId);
        subscription.setUserId(userId);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(Long eventId, Long userId) {
        subscriptionRepository.deleteByEventIdAndUserId(eventId, userId);
    }
}
```

- [ ] **Step 3: 创建 EventRegistrationService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRegistrationRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import com.volleyball.volleyballcommunitybackend.repository.EventRegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;

    public EventRegistrationService(EventRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public EventRegistrationResponse register(Long eventId, Long userId, EventRegistrationRequest request) {
        if (registrationRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("已报名过此赛事");
        }

        EventRegistration reg = new EventRegistration();
        reg.setEventId(eventId);
        reg.setUserId(userId);
        reg.setTeamName(request.getTeamName());
        reg.setContactPerson(request.getContactPerson());
        reg.setContactPhone(request.getContactPhone());
        reg.setTeamSize(request.getTeamSize());
        reg.setStatus("PENDING");

        EventRegistration saved = registrationRepository.save(reg);
        return toResponse(saved);
    }

    public Page<EventRegistrationResponse> getRegistrations(Long eventId, Pageable pageable) {
        return registrationRepository.findByEventId(eventId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void approveRegistration(Long registrationId, Long reviewerId, boolean approved) {
        EventRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));

        reg.setReviewedBy(reviewerId);
        reg.setStatus(approved ? "APPROVED" : "REJECTED");
        registrationRepository.save(reg);
    }

    private EventRegistrationResponse toResponse(EventRegistration reg) {
        EventRegistrationResponse response = new EventRegistrationResponse();
        response.setId(reg.getId());
        response.setEventId(reg.getEventId());
        response.setTeamName(reg.getTeamName());
        response.setContactPerson(reg.getContactPerson());
        response.setContactPhone(maskPhone(reg.getContactPhone()));
        response.setTeamSize(reg.getTeamSize());
        response.setStatus(reg.getStatus());
        response.setReviewedAt(reg.getReviewedAt());
        response.setCreatedAt(reg.getCreatedAt());
        return response;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/
git commit -m "feat: add event calendar service classes"
```

---

## Task 5: 创建 Controller

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/controller/EventController.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/controller/EventRegistrationController.java`

- [ ] **Step 1: 创建 EventController.java**

```java
package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventResponse;
import com.volleyball.volleyballcommunitybackend.service.EventService;
import com.volleyball.volleyballcommunitybackend.service.EventSubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class EventController {

    private final EventService eventService;
    private final EventSubscriptionService subscriptionService;

    public EventController(EventService eventService, EventSubscriptionService subscriptionService) {
        this.eventService = eventService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/api/event")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEventList(
            Pageable pageable,
            Authentication authentication) {
        Long currentUserId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<EventResponse> list = eventService.getEventList(pageable, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/api/event/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = authentication != null ? (Long) authentication.getPrincipal() : null;
        EventResponse event = eventService.getEventById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @PostMapping("/api/event")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        // 比赛类型需要管理员权限，这里简化处理，后续在 SecurityConfig 中控制
        EventResponse event = eventService.createEvent(request, userId);
        return ResponseEntity.ok(ApiResponse.success("创建成功", event));
    }

    @PutMapping("/api/event/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        // 管理员判断需要结合 SecurityConfig
        EventResponse event = eventService.updateEvent(id, request, userId, false);
        return ResponseEntity.ok(ApiResponse.success("更新成功", event));
    }

    @DeleteMapping("/api/event/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PostMapping("/api/event/{id}/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        subscriptionService.subscribe(id, userId);
        return ResponseEntity.ok(ApiResponse.success("订阅成功", null));
    }

    @DeleteMapping("/api/event/{id}/subscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        subscriptionService.unsubscribe(id, userId);
        return ResponseEntity.ok(ApiResponse.success("取消订阅成功", null));
    }
}
```

- [ ] **Step 2: 创建 EventRegistrationController.java**

```java
package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.EventRegistrationRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.service.EventRegistrationService;
import com.volleyball.volleyballcommunitybackend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class EventRegistrationController {

    private final EventRegistrationService registrationService;
    private final EventService eventService;

    public EventRegistrationController(EventRegistrationService registrationService, EventService eventService) {
        this.registrationService = registrationService;
        this.eventService = eventService;
    }

    @PostMapping("/api/event/{id}/register")
    public ResponseEntity<ApiResponse<EventRegistrationResponse>> register(
            @PathVariable Long id,
            @Valid @RequestBody EventRegistrationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        EventRegistrationResponse reg = registrationService.register(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("报名成功，等待审核", reg));
    }

    @GetMapping("/api/event/{id}/registration")
    public ResponseEntity<ApiResponse<Page<EventRegistrationResponse>>> getRegistrations(
            @PathVariable Long id,
            Pageable pageable,
            Authentication authentication) {
        // 权限检查：仅管理员或发布者可查看
        Long userId = (Long) authentication.getPrincipal();
        if (!eventService.isEventOrganizer(id, userId)) {
            throw new RuntimeException("无权限查看报名列表");
        }
        Page<EventRegistrationResponse> list = registrationService.getRegistrations(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/api/event/{id}/registration/{regId}")
    public ResponseEntity<ApiResponse<Void>> approveRegistration(
            @PathVariable Long id,
            @PathVariable Long regId,
            @RequestParam boolean approved,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        // 权限检查
        if (!eventService.isEventOrganizer(id, userId)) {
            throw new RuntimeException("无权限审核报名");
        }
        registrationService.approveRegistration(regId, userId, approved);
        return ResponseEntity.ok(ApiResponse.success("审核成功", null));
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/controller/
git commit -m "feat: add event calendar controller classes"
```

---

## Task 6: 更新 SecurityConfig

**Files:**
- Modify: `src/main/java/com/volleyball/volleyballcommunitybackend/config/SecurityConfig.java`

- [ ] **Step 1: 添加赛事相关接口权限配置**

在 `authorizeHttpRequests` 中添加：
```java
.requestMatchers(HttpMethod.GET, "/api/event").permitAll()
.requestMatchers(HttpMethod.GET, "/api/event/{id}").permitAll()
.requestMatchers(HttpMethod.POST, "/api/event").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/event/{id}").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/event/{id}").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/api/event/{id}/subscribe").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/event/{id}/subscribe").authenticated()
.requestMatchers(HttpMethod.POST, "/api/event/{id}/register").authenticated()
.requestMatchers(HttpMethod.GET, "/api/event/{id}/registration").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/event/{id}/registration/{regId}").authenticated()
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/config/SecurityConfig.java
git commit -m "feat: add event API permissions to SecurityConfig"
```

---

## Task 7: 更新 SSE 推送支持赛事事件

**Files:**
- Modify: `src/main/java/com/volleyball/volleyballcommunitybackend/service/SseService.java`

- [ ] **Step 1: 添加赛事推送方法**

在 SseService 中添加：
```java
public void sendEventUpdate(Long userId, Object data) {
    sendMessageToUser(userId, "eventUpdate", data);
}

public void sendEventStatusChanged(Long userId, Object data) {
    sendMessageToUser(userId, "eventStatusChanged", data);
}

public void sendNewRegistration(Long organizerId, Object data) {
    sendMessageToUser(organizerId, "newRegistration", data);
}

public void sendRegistrationResult(Long userId, Object data) {
    sendMessageToUser(userId, "registrationResult", data);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/SseService.java
git commit -m "feat: add SSE methods for event notifications"
```

---

## Task 8: 更新 API 文档

**Files:**
- Modify: `docs/api.md`
- Modify: `docs/project.md`

- [ ] **Step 1: 在 api.md 中添加赛事相关 API 文档**

在文件末尾添加赛事模块章节：
```markdown
## 赛事模块 /api/event

### 获取赛事列表

```
GET /api/event
```

[完整的 API 文档内容...]
```

- [ ] **Step 2: 在 project.md 中更新开发进度**

在开发进度部分添加：
```markdown
### 阶段四：赛事日历 🔄 开发中
- [x] 赛事发布（event）
- [ ] 赛事订阅（event_subscription）
- [ ] 赛事报名（event_registration）
```

- [ ] **Step 3: Commit**

```bash
git add docs/api.md docs/project.md
git commit -m "docs: update API docs and project status for event calendar"
```

---

## Task 9: 最终检查和测试

- [ ] **Step 1: 编译检查**

```bash
./mvnw compile
```

- [ ] **Step 2: 运行测试**

```bash
./mvnw test
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: complete phase 4 event calendar feature"
```
